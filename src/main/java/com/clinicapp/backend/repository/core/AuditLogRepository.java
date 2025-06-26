package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find logs by user
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Find logs by entity
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    // Find logs by action
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    // Find logs by severity
    Page<AuditLog> findBySeverityOrderByCreatedAtDesc(AuditLog.AuditSeverity severity, Pageable pageable);

    // Find logs within a date range
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Find recent critical logs
    @Query("SELECT a FROM AuditLog a WHERE a.severity = 'CRITICAL' AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findCriticalLogsSince(@Param("since") LocalDateTime since);

    // Action statistics
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since GROUP BY a.action")
    List<Object[]> getActionStats(@Param("since") LocalDateTime since);
}