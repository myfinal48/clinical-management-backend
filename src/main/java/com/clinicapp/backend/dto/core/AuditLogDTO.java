package com.clinicapp.backend.dto.core;

import com.clinicapp.backend.model.core.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userRole;
    private String action;
    private String entityType;
    private Long entityId;
    private String oldValues;
    private String newValues;
    private String ipAddress;
    private String userAgent;
    private String requestUrl;
    private String requestMethod;
    private String details;
    private AuditLog.AuditSeverity severity;
    private LocalDateTime createdAt;

    /**
     * Convert AuditLog entity to AuditLogDTO
     */
    public static AuditLogDTO fromEntity(AuditLog auditLog) {
        if (auditLog == null)
            return null;

        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .userRole(auditLog.getUserRole())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .oldValues(auditLog.getOldValues())
                .newValues(auditLog.getNewValues())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestUrl(auditLog.getRequestUrl())
                .requestMethod(auditLog.getRequestMethod())
                .details(auditLog.getDetails())
                .severity(auditLog.getSeverity())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}