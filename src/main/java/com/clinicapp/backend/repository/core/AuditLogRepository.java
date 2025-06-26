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

    // Trouver les logs par utilisateur
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Trouver les logs par entité
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    // Trouver les logs par action
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    // Trouver les logs par sévérité
    Page<AuditLog> findBySeverityOrderByCreatedAtDesc(AuditLog.AuditSeverity severity, Pageable pageable);

    // Trouver les logs dans une période
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Trouver les logs critiques récents
    @Query("SELECT a FROM AuditLog a WHERE a.severity = 'CRITICAL' AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findCriticalLogsSince(@Param("since") LocalDateTime since);

    // Statistiques par action
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since GROUP BY a.action")
    List<Object[]> getActionStats(@Param("since") LocalDateTime since);
}