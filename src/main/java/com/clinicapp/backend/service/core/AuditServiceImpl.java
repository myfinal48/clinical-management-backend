package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.AuditLogDTO;
import com.clinicapp.backend.model.core.AuditLog;
import com.clinicapp.backend.repository.core.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
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
            log.debug("Audit log created: {} by user {}", action, username);

        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    @Override
    @Transactional
    public void logAction(Long userId, String username, String userRole, String action,
            String entityType, Long entityId, String details) {
        logAction(userId, username, userRole, action, entityType, entityId, details, AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional
    public void logLogin(Long userId, String username, String userRole, String ipAddress, String userAgent) {
        logAction(userId, username, userRole, "USER_LOGIN", "USER", userId,
                "User logged in from IP: " + ipAddress, AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional
    public void logLogout(Long userId, String username, String userRole) {
        logAction(userId, username, userRole, "USER_LOGOUT", "USER", userId,
                "User logged out", AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional
    public void logRegistration(Long userId, String username, String userRole) {
        logAction(userId, username, userRole, "USER_REGISTRATION", "USER", userId,
                "New user registered", AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional
    public void logPasswordChange(Long userId, String username, String userRole) {
        logAction(userId, username, userRole, "PASSWORD_CHANGE", "USER", userId,
                "Password changed", AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional
    public void logFailedLogin(String email, String ipAddress, String userAgent, String reason) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(email)
                    .userRole("UNKNOWN")
                    .action("LOGIN_FAILED")
                    .entityType("USER")
                    .details("Failed login attempt from IP: " + ipAddress + ". Reason: " + reason)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .severity(AuditLog.AuditSeverity.WARNING)
                    .build();

            auditLogRepository.save(auditLog);
            log.warn("Failed login attempt for user: {} from IP: {}", email, ipAddress);

        } catch (Exception e) {
            log.error("Failed to log failed login attempt", e);
        }
    }

    @Override
    @Transactional
    public void logCreation(Long userId, String username, String userRole,
            String entityType, Long entityId, Object newData) {
        String details = "Created: " + (newData != null ? newData.toString() : "null");
        logAction(userId, username, userRole, "CREATE", entityType, entityId, details);
    }

    @Override
    @Transactional
    public void logUpdate(Long userId, String username, String userRole,
            String entityType, Long entityId, Object oldData, Object newData) {
        String details = "Updated from: " + (oldData != null ? oldData.toString() : "null") +
                " to: " + (newData != null ? newData.toString() : "null");
        logAction(userId, username, userRole, "UPDATE", entityType, entityId, details);
    }

    @Override
    @Transactional
    public void logDeletion(Long userId, String username, String userRole,
            String entityType, Long entityId, Object deletedData) {
        String details = "Deleted: " + (deletedData != null ? deletedData.toString() : "null");
        logAction(userId, username, userRole, "DELETE", entityType, entityId, details);
    }

    @Override
    @Transactional
    public void logNotificationSent(Long senderId, Long recipientId, String notificationType, String details) {
        logAction(senderId, "SYSTEM", "SYSTEM", "NOTIFICATION_SENT", "NOTIFICATION", recipientId, details);
    }

    @Override
    @Transactional
    public void logSecurityEvent(Long userId, String username, String userRole, String action, String details) {
        logAction(userId, username, userRole, action, "SECURITY", null, details, AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional
    public void logSystemError(String action, String details, Exception exception) {
        try {
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
        } catch (Exception e) {
            log.error("Failed to log system error", e);
        }
    }

    @Override
    @Transactional
    public void logUnauthorizedAccess(String username, String ipAddress, String resource) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action("UNAUTHORIZED_ACCESS")
                    .entityType("SECURITY")
                    .details("Unauthorized access attempt to: " + resource)
                    .severity(AuditLog.AuditSeverity.WARNING)
                    .ipAddress(ipAddress)
                    .userAgent(getUserAgent())
                    .requestUrl(getRequestUrl())
                    .requestMethod(getRequestMethod())
                    .build();

            auditLogRepository.save(auditLog);
            log.warn("Unauthorized access attempt by {} from {} to {}", username, ipAddress, resource);
        } catch (Exception e) {
            log.error("Failed to log unauthorized access", e);
        }
    }

    @Override
    @Transactional
    public void logAppointmentAction(Long userId, String username, String userRole, String action, Long appointmentId,
            String details) {
        logAction(userId, username, userRole, action, "APPOINTMENT", appointmentId, details,
                AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional
    public void logPrescriptionAction(Long userId, String username, String userRole, String action, Long prescriptionId,
            String details) {
        logAction(userId, username, userRole, action, "PRESCRIPTION", prescriptionId, details,
                AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional
    public void logInvoiceAction(Long userId, String username, String userRole, String action, Long invoiceId,
            String details) {
        logAction(userId, username, userRole, action, "INVOICE", invoiceId, details, AuditLog.AuditSeverity.INFO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action)
                .stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogsBySeverity(AuditLog.AuditSeverity severity) {
        return auditLogRepository.findBySeverityOrderByCreatedAtDesc(severity)
                .stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getCriticalLogsSince(LocalDateTime since) {
        return auditLogRepository.findCriticalLogsSince(since)
                .stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getActionStatistics(LocalDateTime since) {
        return auditLogRepository.getActionStatistics(since);
    }

    // Utility methods to retrieve request information
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