package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.AuditLog;
import com.backend.tutor_app.model.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des logs d'audit
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Trouve les logs par action avec pagination
     */
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    /**
     * Trouve les logs par utilisateur avec pagination
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Trouve les logs par admin avec pagination
     */
    Page<AuditLog> findByAdminId(Long adminId, Pageable pageable);

    /**
     * Trouve les logs dans une période donnée
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Trouve les logs par type d'entité
     */
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Trouve les logs par entité spécifique
     */
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    /**
     * Trouve les logs par niveau de sévérité
     */
    Page<AuditLog> findBySeverityLevelGreaterThanEqual(Integer severityLevel, Pageable pageable);

    /**
     * Trouve les logs d'échec
     */
    Page<AuditLog> findBySuccessFalse(Pageable pageable);

    /**
     * Trouve les logs avec filtres avancés
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:userId IS NULL OR al.userId = :userId) AND " +
           "(:adminId IS NULL OR al.adminId = :adminId) AND " +
           "(:entityType IS NULL OR al.entityType = :entityType) AND " +
           "(:entityId IS NULL OR al.entityId = :entityId) AND " +
           "(:severityLevel IS NULL OR al.severityLevel >= :severityLevel) AND " +
           "(:success IS NULL OR al.success = :success) AND " +
           "(:startDate IS NULL OR al.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR al.timestamp <= :endDate)")
    Page<AuditLog> findWithFilters(
        @Param("action") AuditAction action,
        @Param("userId") Long userId,
        @Param("adminId") Long adminId,
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId,
        @Param("severityLevel") Integer severityLevel,
        @Param("success") Boolean success,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Statistiques des actions par mois
     * Note: Cette requête utilise des fonctions natives SQL (YEAR, MONTH), donc on utilise nativeQuery=true
     */
    @Query(value = "SELECT YEAR(timestamp) as year, MONTH(timestamp) as month, COUNT(*) as count " +
           "FROM audit_log " +
           "WHERE timestamp >= :startDate " +
           "GROUP BY YEAR(timestamp), MONTH(timestamp) " +
           "ORDER BY year DESC, month DESC", 
           nativeQuery = true)
    List<Object[]> getActionStatsByMonth(@Param("startDate") LocalDateTime startDate);

    /**
     * Compte les logs par action
     */
    @Query("SELECT al.action, COUNT(al) as actionCount FROM AuditLog al GROUP BY al.action ORDER BY actionCount DESC")
    List<Object[]> countLogsByAction();

    /**
     * Compte les logs par niveau de sévérité
     */
    @Query("SELECT al.severityLevel, COUNT(al) FROM AuditLog al GROUP BY al.severityLevel ORDER BY al.severityLevel")
    List<Object[]> countLogsBySeverityLevel();

    /**
     * Trouve les logs d'erreur récents
     */
    @Query("SELECT al FROM AuditLog al WHERE al.severityLevel >= 3 AND al.timestamp >= :recentCutoff ORDER BY al.timestamp DESC")
    List<AuditLog> findRecentErrors(@Param("recentCutoff") LocalDateTime recentCutoff);

    /**
     * Trouve les logs d'activité suspecte
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.action IN ('SECURITY_VIOLATION', 'SUSPICIOUS_ACTIVITY', 'RATE_LIMIT_EXCEEDED') OR " +
           "(al.action = 'LOGIN_FAILED' AND al.timestamp >= :recentCutoff)")
    List<AuditLog> findSuspiciousActivity(@Param("recentCutoff") LocalDateTime recentCutoff);

    /**
     * Trouve les logs par adresse IP
     */
    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress ORDER BY al.timestamp DESC")
    Page<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress, Pageable pageable);

    /**
     * Trouve les logs par session
     */
    @Query("SELECT al FROM AuditLog al WHERE al.sessionId = :sessionId ORDER BY al.timestamp ASC")
    List<AuditLog> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * Statistiques des utilisateurs les plus actifs
     */
    @Query("SELECT al.userId, COUNT(al) as actionCount " +
           "FROM AuditLog al " +
           "WHERE al.userId IS NOT NULL AND al.timestamp >= :startDate " +
           "GROUP BY al.userId " +
           "ORDER BY actionCount DESC")
    List<Object[]> findMostActiveUsers(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * Statistiques des admins les plus actifs
     */
    @Query("SELECT al.adminId, COUNT(al) as actionCount " +
           "FROM AuditLog al " +
           "WHERE al.adminId IS NOT NULL AND al.timestamp >= :startDate " +
           "GROUP BY al.adminId " +
           "ORDER BY actionCount DESC")
    List<Object[]> findMostActiveAdmins(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * Recherche textuelle dans les logs
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "LOWER(al.details) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(al.errorMessage) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(al.entityType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(al.ipAddress) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<AuditLog> searchLogs(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Trouve les logs de performance lente
     */
    @Query("SELECT al FROM AuditLog al WHERE al.executionTimeMs > :thresholdMs ORDER BY al.executionTimeMs DESC")
    List<AuditLog> findSlowOperations(@Param("thresholdMs") Long thresholdMs, Pageable pageable);

    /**
     * Nettoie les anciens logs (pour maintenance)
     * Note: @Modifying est requis pour les requêtes DELETE/UPDATE
     */
    @Modifying
    @Query("DELETE FROM AuditLog al WHERE al.timestamp < :cutoffDate AND al.severityLevel < 3")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Compte les logs par jour pour les graphiques
     * Note: Cette requête utilise une fonction native SQL, donc on utilise nativeQuery=true
     */
    @Query(value = "SELECT DATE(timestamp) as logDate, COUNT(*) as count " +
           "FROM audit_log " +
           "WHERE timestamp >= :startDate " +
           "GROUP BY DATE(timestamp) " +
           "ORDER BY logDate DESC", 
           nativeQuery = true)
    List<Object[]> getLogCountsByDay(@Param("startDate") LocalDateTime startDate);

    /**
     * Trouve les pics d'activité (plus de X actions par minute)
     * Note: Cette requête utilise une fonction native SQL, donc on utilise nativeQuery=true
     */
    @Query(value = "SELECT DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i') as minute, COUNT(*) as count " +
           "FROM audit_log " +
           "WHERE timestamp >= :startDate " +
           "GROUP BY DATE_FORMAT(timestamp, '%Y-%m-%d %H:%i') " +
           "HAVING count > :threshold " +
           "ORDER BY count DESC", 
           nativeQuery = true)
    List<Object[]> findActivitySpikes(@Param("startDate") LocalDateTime startDate, @Param("threshold") Long threshold);
}
