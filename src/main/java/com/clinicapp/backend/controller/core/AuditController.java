package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.AuditLogDTO;
import com.clinicapp.backend.model.core.AuditLog;
import com.clinicapp.backend.service.core.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Endpoints for retrieving audit logs")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/user")
    @Operation(summary = "Get audit logs by user", description = "Retrieve paginated audit logs for a specific user.")
    public Page<AuditLogDTO> getAuditLogsByUser(
            @Parameter(description = "User ID") @RequestParam Long userId,
            @Parameter(description = "Pagination parameters", 
                      examples = @ExampleObject(name = "Default pagination", value = "{\"page\": 0, \"size\": 5, \"sort\": [\"createdAt,desc\"]}"))
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return auditService.getAuditLogsByUser(userId, pageable);
    }

    @GetMapping("/entity")
    @Operation(summary = "Get audit logs by entity", description = "Retrieve paginated audit logs for a specific entity type and ID.")
    public Page<AuditLogDTO> getAuditLogsByEntity(
            @Parameter(description = "Entity type", 
                      schema = @Schema(type = "string", 
                                     allowableValues = {"USER", "APPOINTMENT", "PRESCRIPTION", "INVOICE", "PATIENT", "NOTIFICATION", "SECURITY", "SYSTEM"}))
            @RequestParam String entityType,
            @Parameter(description = "Entity ID") @RequestParam Long entityId,
            @Parameter(description = "Pagination parameters", 
                      examples = @ExampleObject(name = "Default pagination", value = "{\"page\": 0, \"size\": 5, \"sort\": [\"createdAt,desc\"]}"))
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return auditService.getAuditLogsByEntity(entityType, entityId, pageable);
    }

    @GetMapping("/action")
    @Operation(summary = "Get audit logs by action", description = "Retrieve paginated audit logs for a specific action.")
    public Page<AuditLogDTO> getAuditLogsByAction(
            @Parameter(description = "Action type", 
                      schema = @Schema(type = "string", 
                                     allowableValues = {"CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "UNAUTHORIZED_ACCESS", "NOTIFICATION_SENT"}))
            @RequestParam String action,
            @Parameter(description = "Pagination parameters", 
                      examples = @ExampleObject(name = "Default pagination", value = "{\"page\": 0, \"size\": 5, \"sort\": [\"createdAt,desc\"]}"))
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return auditService.getAuditLogsByAction(action, pageable);
    }

    @GetMapping("/severity")
    @Operation(summary = "Get audit logs by severity", description = "Retrieve paginated audit logs for a specific severity level.")
    public Page<AuditLogDTO> getAuditLogsBySeverity(
            @Parameter(description = "Severity level (INFO, WARNING, ERROR, CRITICAL)") @RequestParam AuditLog.AuditSeverity severity,
            @Parameter(description = "Pagination parameters", 
                      examples = @ExampleObject(name = "Default pagination", value = "{\"page\": 0, \"size\": 5, \"sort\": [\"createdAt,desc\"]}"))
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return auditService.getAuditLogsBySeverity(severity, pageable);
    }
}