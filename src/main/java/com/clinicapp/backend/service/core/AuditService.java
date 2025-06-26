package com.clinicapp.backend.service.core;

import com.clinicapp.backend.model.core.AuditLog;
import com.clinicapp.backend.dto.core.AuditLogDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditService {

        /**
         * Records a generic audit action
         */
        void logAction(Long userId, String username, String userRole, String action,
                        String entityType, Long entityId, String details,
                        AuditLog.AuditSeverity severity);

        /**
         * Records a generic audit action with default INFO severity
         */
        void logAction(Long userId, String username, String userRole, String action,
                        String entityType, Long entityId, String details);

        /**
         * Records a user login
         */
        void logLogin(Long userId, String username, String userRole, String ipAddress, String userAgent);

        /**
         * Records a user logout
         */
        void logLogout(Long userId, String username, String userRole);

        /**
         * Records a user registration
         */
        void logRegistration(Long userId, String username, String userRole);

        /**
         * Records a password change
         */
        void logPasswordChange(Long userId, String username, String userRole);

        /**
         * Records a failed login attempt
         */
        void logFailedLogin(String email, String ipAddress, String userAgent, String reason);

        /**
         * Records an entity creation
         */
        void logCreation(Long userId, String username, String userRole,
                        String entityType, Long entityId, Object newData);

        /**
         * Records an entity modification
         */
        void logUpdate(Long userId, String username, String userRole,
                        String entityType, Long entityId, Object oldData, Object newData);

        /**
         * Records an entity deletion
         */
        void logDeletion(Long userId, String username, String userRole,
                        String entityType, Long entityId, Object deletedData);

        /**
         * Records a security action (login, logout, etc.)
         */
        void logSecurityEvent(Long userId, String username, String userRole,
                        String action, String details);

        /**
         * Records a system error
         */
        void logSystemError(String action, String details, Exception exception);

        /**
         * Records an unauthorized access attempt
         */
        void logUnauthorizedAccess(String username, String ipAddress, String resource);

        /**
         * Records a notification action
         */
        void logNotificationSent(Long senderId, Long recipientId, String notificationType, String details);

        /**
         * Records an appointment action
         */
        void logAppointmentAction(Long userId, String username, String userRole,
                        String action, Long appointmentId, String details);

        /**
         * Records a prescription action
         */
        void logPrescriptionAction(Long userId, String username, String userRole,
                        String action, Long prescriptionId, String details);

        /**
         * Records an invoice action
         */
        void logInvoiceAction(Long userId, String username, String userRole,
                        String action, Long invoiceId, String details);

        /**
         * Get audit logs by user
         */
        List<AuditLogDTO> getAuditLogsByUser(Long userId);

        /**
         * Get audit logs by entity
         */
        List<AuditLogDTO> getAuditLogsByEntity(String entityType, Long entityId);

        /**
         * Get audit logs by action
         */
        List<AuditLogDTO> getAuditLogsByAction(String action);

        /**
         * Get audit logs by severity
         */
        List<AuditLogDTO> getAuditLogsBySeverity(AuditLog.AuditSeverity severity);

        /**
         * Get audit logs by date range
         */
        List<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

        /**
         * Get critical audit logs since a specific date
         */
        List<AuditLogDTO> getCriticalLogsSince(LocalDateTime since);

        /**
         * Get action statistics
         */
        List<Object[]> getActionStatistics(LocalDateTime since);
}
