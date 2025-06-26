package com.clinicapp.backend.dto.core;

import com.clinicapp.backend.model.core.AuditLog;
import lombok.Data;
import java.time.LocalDateTime;

@Data
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
}