package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.AuditLogDTO;
import com.clinicapp.backend.model.core.AuditAction;
import com.clinicapp.backend.model.core.AuditLog;
import com.clinicapp.backend.service.core.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log operations")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user", description = "Get all audit logs for a specific user")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.getAuditLogsByUser(userId));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit logs by entity", description = "Get all audit logs for a specific entity")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditService.getAuditLogsByEntity(entityType, entityId));
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get audit logs by action", description = "Get all audit logs for a specific action")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsByAction(@PathVariable AuditAction action) {
        return ResponseEntity.ok(auditService.getAuditLogsByAction(action.name()));
    }


    @GetMapping("/severity/{severity}")
    @Operation(summary = "Get audit logs by severity", description = "Get all audit logs for a specific severity level")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsBySeverity(@PathVariable AuditLog.AuditSeverity severity) {
        return ResponseEntity.ok(auditService.getAuditLogsBySeverity(severity));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs by date range", description = "Get all audit logs within a date range")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(auditService.getAuditLogsByDateRange(startDate, endDate));
    }

    @GetMapping("/critical")
    @Operation(summary = "Get critical audit logs", description = "Get all critical audit logs since a specific date")
    public ResponseEntity<List<AuditLogDTO>> getCriticalLogsSince(@RequestParam LocalDateTime since) {
        return ResponseEntity.ok(auditService.getCriticalLogsSince(since));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get action statistics", description = "Get action statistics since a specific date")
    public ResponseEntity<List<Object[]>> getActionStatistics(@RequestParam LocalDateTime since) {
        return ResponseEntity.ok(auditService.getActionStatistics(since));
    }
}