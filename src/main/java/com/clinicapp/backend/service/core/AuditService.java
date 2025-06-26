package com.clinicapp.backend.service.core;

import com.clinicapp.backend.model.core.AuditLog;

public interface AuditService {

    /**
     * Enregistre une action d'audit générique
     */
    void logAction(Long userId, String username, String userRole, String action,
            String entityType, Long entityId, String details,
            AuditLog.AuditSeverity severity);

    /**
     * Enregistre une création d'entité
     */
    void logCreation(Long userId, String username, String userRole,
            String entityType, Long entityId, Object newData);

    /**
     * Enregistre une modification d'entité
     */
    void logUpdate(Long userId, String username, String userRole,
            String entityType, Long entityId, Object oldData, Object newData);

    /**
     * Enregistre une suppression d'entité
     */
    void logDeletion(Long userId, String username, String userRole,
            String entityType, Long entityId, Object deletedData);

    /**
     * Enregistre une action de sécurité (connexion, déconnexion, etc.)
     */
    void logSecurityEvent(Long userId, String username, String userRole,
            String action, String details);

    /**
     * Enregistre une erreur système
     */
    void logSystemError(String action, String details, Exception exception);

    /**
     * Enregistre une tentative d'accès non autorisé
     */
    void logUnauthorizedAccess(String username, String ipAddress, String resource);

    /**
     * Enregistre une action de notification
     */
    void logNotificationSent(Long senderId, Long recipientId, String notificationType, String details);

    /**
     * Enregistre une action de rendez-vous
     */
    void logAppointmentAction(Long userId, String username, String userRole,
            String action, Long appointmentId, String details);

    /**
     * Enregistre une action de prescription
     */
    void logPrescriptionAction(Long userId, String username, String userRole,
            String action, Long prescriptionId, String details);

    /**
     * Enregistre une action de facture
     */
    void logInvoiceAction(Long userId, String username, String userRole,
            String action, Long invoiceId, String details);
}


