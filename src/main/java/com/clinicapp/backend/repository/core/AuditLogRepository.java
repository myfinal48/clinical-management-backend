package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find audit logs by user
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find audit logs by entity
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    // Find audit logs by action
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    // Find audit logs by severity
    List<AuditLog> findBySeverityOrderByCreatedAtDesc(AuditLog.AuditSeverity severity);

    // Find audit logs by date range
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find critical audit logs since a specific date
    @Query("SELECT a FROM AuditLog a WHERE a.severity = 'CRITICAL' AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findCriticalLogsSince(@Param("since") LocalDateTime since);

    // Get action statistics
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since GROUP BY a.action")
    List<Object[]> getActionStatistics(@Param("since") LocalDateTime since);
}