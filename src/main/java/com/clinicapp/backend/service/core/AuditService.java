package com.clinicapp.backend.service.core;

import com.clinicapp.backend.model.core.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.clinicapp.backend.dto.core.AuditLogDTO;

public interface AuditService {

        /**
         * Records a generic audit action
         */
        void logAction(Long userId, String username, String userRole, String action,
                        String entityType, Long entityId, String details,
                        AuditLog.AuditSeverity severity);

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

        Page<AuditLogDTO> getAuditLogsByUser(Long userId, Pageable pageable);

        Page<AuditLogDTO> getAuditLogsByEntity(String entityType, Long entityId, Pageable pageable);

        Page<AuditLogDTO> getAuditLogsByAction(String action, Pageable pageable);

        Page<AuditLogDTO> getAuditLogsBySeverity(AuditLog.AuditSeverity severity, Pageable pageable);
}
