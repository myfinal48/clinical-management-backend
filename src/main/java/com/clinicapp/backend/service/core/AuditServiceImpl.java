package com.clinicapp.backend.service.core;

import com.clinicapp.backend.model.core.AuditLog;
import com.clinicapp.backend.repository.core.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(Long userId, String username, String userRole, String action,
            String entityType, Long entityId, String details,
            AuditLog.AuditSeverity severity) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .userRole(userRole)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .severity(severity)
                    .ipAddress(getClientIpAddress())
                    .userAgent(getUserAgent())
                    .requestUrl(getRequestUrl())
                    .requestMethod(getRequestMethod())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} - {} - {} by user: {}", action, entityType, entityId, username);

        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    @Override
    public void logCreation(Long userId, String username, String userRole,
            String entityType, Long entityId, Object newData) {
        String details = "Created: " + (newData != null ? newData.toString() : "null");
        logAction(userId, username, userRole, "CREATE", entityType, entityId,
                details, AuditLog.AuditSeverity.INFO);
    }

    @Override
    public void logUpdate(Long userId, String username, String userRole,
            String entityType, Long entityId, Object oldData, Object newData) {
        String details = String.format("Updated: old=%s, new=%s",
                oldData != null ? oldData.toString() : "null",
                newData != null ? newData.toString() : "null");
        logAction(userId, username, userRole, "UPDATE", entityType, entityId,
                details, AuditLog.AuditSeverity.INFO);
    }

    @Override
    public void logDeletion(Long userId, String username, String userRole,
            String entityType, Long entityId, Object deletedData) {
        String details = "Deleted: " + (deletedData != null ? deletedData.toString() : "null");
        logAction(userId, username, userRole, "DELETE", entityType, entityId,
                details, AuditLog.AuditSeverity.WARNING);
    }

    @Override
    public void logSecurityEvent(Long userId, String username, String userRole,
            String action, String details) {
        logAction(userId, username, userRole, action, "SECURITY", null,
                details, AuditLog.AuditSeverity.ERROR);
    }

    @Override
    public void logSystemError(String action, String details, Exception exception) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType("SYSTEM")
                .details(details + " - Exception: " + exception.getMessage())
                .severity(AuditLog.AuditSeverity.CRITICAL)
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .requestUrl(getRequestUrl())
                .requestMethod(getRequestMethod())
                .build();

        auditLogRepository.save(auditLog);
        log.error("System error logged: {} - {}", action, details, exception);
    }

    @Override
    public void logUnauthorizedAccess(String username, String ipAddress, String resource) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action("UNAUTHORIZED_ACCESS")
                .entityType("SECURITY")
                .details("Unauthorized access attempt to: " + resource)
                .severity(AuditLog.AuditSeverity.ERROR)
                .ipAddress(ipAddress)
                .userAgent(getUserAgent())
                .requestUrl(getRequestUrl())
                .requestMethod(getRequestMethod())
                .build();

        auditLogRepository.save(auditLog);
        log.warn("Unauthorized access attempt by {} from {} to {}", username, ipAddress, resource);
    }

    @Override
    public void logNotificationSent(Long senderId, Long recipientId, String notificationType, String details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(senderId)
                .action("NOTIFICATION_SENT")
                .entityType("NOTIFICATION")
                .entityId(recipientId)
                .details("Notification type: " + notificationType + " - " + details)
                .severity(AuditLog.AuditSeverity.INFO)
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .requestUrl(getRequestUrl())
                .requestMethod(getRequestMethod())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Notification sent from {} to {}: {}", senderId, recipientId, notificationType);
    }

    @Override
    public void logAppointmentAction(Long userId, String username, String userRole,
            String action, Long appointmentId, String details) {
        logAction(userId, username, userRole, action, "APPOINTMENT", appointmentId,
                details, AuditLog.AuditSeverity.INFO);
    }

    @Override
    public void logPrescriptionAction(Long userId, String username, String userRole,
            String action, Long prescriptionId, String details) {
        logAction(userId, username, userRole, action, "PRESCRIPTION", prescriptionId,
                details, AuditLog.AuditSeverity.INFO);
    }

    @Override
    public void logInvoiceAction(Long userId, String username, String userRole,
            String action, Long invoiceId, String details) {
        logAction(userId, username, userRole, action, "INVOICE", invoiceId,
                details, AuditLog.AuditSeverity.INFO);
    }

    // Méthodes utilitaires pour récupérer les informations de la requête
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Could not get client IP address", e);
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Could not get user agent", e);
        }
        return "unknown";
    }

    private String getRequestUrl() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getRequestURL().toString();
            }
        } catch (Exception e) {
            log.warn("Could not get request URL", e);
        }
        return "unknown";
    }

    private String getRequestMethod() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getMethod();
            }
        } catch (Exception e) {
            log.warn("Could not get request method", e);
        }
        return "unknown";
    }
}