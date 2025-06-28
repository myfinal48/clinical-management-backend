package com.clinicapp.backend.aspect;

import com.clinicapp.backend.annotation.Auditable;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.core.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    // Audit CREATE operations
    @AfterReturning(pointcut = "execution(* com.clinicapp.backend.service..*.create*(..))", returning = "result")
    public void auditCreate(JoinPoint joinPoint, Object result) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null && result != null) {
                String entityType = extractEntityType(joinPoint.getTarget().getClass().getSimpleName());
                Long entityId = extractEntityId(result);
                
                auditService.logCreation(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getRole().name(),
                    entityType,
                    entityId,
                    result
                );
            }
        } catch (Exception e) {
            log.warn("Failed to audit create operation", e);
        }
    }

    // Audit UPDATE operations
    @AfterReturning(pointcut = "execution(* com.clinicapp.backend.service..*.update*(..))", returning = "result")
    public void auditUpdate(JoinPoint joinPoint, Object result) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null && result != null) {
                String entityType = extractEntityType(joinPoint.getTarget().getClass().getSimpleName());
                Long entityId = extractEntityId(result);
                
                auditService.logUpdate(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getRole().name(),
                    entityType,
                    entityId,
                    null, // Old data - could be enhanced to capture this
                    result
                );
            }
        } catch (Exception e) {
            log.warn("Failed to audit update operation", e);
        }
    }

    // Audit DELETE operations
    @Before("execution(* com.clinicapp.backend.service..*.delete*(..))")
    public void auditDelete(JoinPoint joinPoint) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                String entityType = extractEntityType(joinPoint.getTarget().getClass().getSimpleName());
                Object[] args = joinPoint.getArgs();
                Long entityId = args.length > 0 && args[0] instanceof Long ? (Long) args[0] : null;
                
                auditService.logDeletion(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getRole().name(),
                    entityType,
                    entityId,
                    null // Could be enhanced to capture the entity before deletion
                );
            }
        } catch (Exception e) {
            log.warn("Failed to audit delete operation", e);
        }
    }

    // Audit specific business operations
    @AfterReturning(pointcut = "execution(* com.clinicapp.backend.service..*.markAsPaid(..))", returning = "result")
    public void auditPayment(JoinPoint joinPoint, Object result) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                Object[] args = joinPoint.getArgs();
                Long invoiceId = args.length > 0 && args[0] instanceof Long ? (Long) args[0] : null;
                
                auditService.logInvoiceAction(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getRole().name(),
                    "PAYMENT_PROCESSED",
                    invoiceId,
                    "Invoice marked as paid"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to audit payment operation", e);
        }
    }

    // Audit methods annotated with @Auditable
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void auditAnnotatedMethod(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                String action = auditable.action().isEmpty() ? 
                    joinPoint.getSignature().getName().toUpperCase() : auditable.action();
                String entityType = auditable.entityType().isEmpty() ? 
                    extractEntityType(joinPoint.getTarget().getClass().getSimpleName()) : auditable.entityType();
                
                Long entityId = null;
                if (result != null && auditable.logResult()) {
                    entityId = extractEntityId(result);
                }
                
                String details = buildAuditDetails(joinPoint, auditable, result);
                
                auditService.logAction(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getRole().name(),
                    action,
                    entityType,
                    entityId,
                    details
                );
            }
        } catch (Exception e) {
            log.warn("Failed to audit annotated method", e);
        }
    }

    private String buildAuditDetails(JoinPoint joinPoint, Auditable auditable, Object result) {
        StringBuilder details = new StringBuilder();
        
        if (auditable.logParameters()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            details.append("Parameters: ");
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                if (i > 0) details.append(", ");
                details.append(paramNames[i]).append("=").append(args[i]);
            }
        }
        
        if (auditable.logResult() && result != null) {
            if (details.length() > 0) details.append("; ");
            details.append("Result: ").append(result.toString());
        }
        
        return details.toString();
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
        } catch (Exception e) {
            log.debug("Could not get current user for audit", e);
        }
        return null;
    }

    private String extractEntityType(String serviceClassName) {
        // Convert service class name to entity type
        // e.g., "PatientServiceImpl" -> "PATIENT"
        return serviceClassName
                .replace("ServiceImpl", "")
                .replace("Service", "")
                .toUpperCase();
    }

    private Long extractEntityId(Object entity) {
        try {
            // Use reflection to get ID field
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(entity);
            return id instanceof Long ? (Long) id : null;
        } catch (Exception e) {
            log.debug("Could not extract entity ID", e);
            return null;
        }
    }
}