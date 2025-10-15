package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.ModerationReport;
import com.backend.tutor_app.model.enums.ReportStatus;
import com.backend.tutor_app.model.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des signalements de modération
 */
@Repository
public interface ModerationReportRepository extends JpaRepository<ModerationReport, Long> {

    /**
     * Trouve les signalements par statut avec pagination
     */
    Page<ModerationReport> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * Trouve les signalements par type avec pagination
     */
    Page<ModerationReport> findByType(ReportType type, Pageable pageable);

    /**
     * Trouve les signalements par type et statut
     */
    Page<ModerationReport> findByTypeAndStatus(
        ReportType type, 
        ReportStatus status, 
        Pageable pageable
    );

    /**
     * Trouve tous les signalements concernant un utilisateur
     */
    List<ModerationReport> findByReportedUserId(Long reportedUserId);

    /**
     * Trouve les signalements créés par un utilisateur
     */
    List<ModerationReport> findByReporterId(Long reporterId);

    /**
     * Compte les signalements par statut
     */
    @Query("SELECT COUNT(mr) FROM ModerationReport mr WHERE mr.status = :status")
    long countByStatus(@Param("status") ReportStatus status);

    /**
     * Compte les signalements par type
     */
    @Query("SELECT COUNT(mr) FROM ModerationReport mr WHERE mr.type = :type")
    long countByType(@Param("type") ReportType type);

    /**
     * Trouve les signalements en attente depuis plus de X jours
     */
    @Query("SELECT mr FROM ModerationReport mr WHERE mr.status = 'PENDING' AND mr.reportedAt < :cutoffDate")
    List<ModerationReport> findPendingReportsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Trouve les signalements haute priorité non résolus
     */
    @Query("SELECT mr FROM ModerationReport mr WHERE mr.priorityLevel >= 3 AND mr.status IN ('PENDING', 'UNDER_REVIEW')")
    List<ModerationReport> findHighPriorityUnresolvedReports();

    /**
     * Trouve les signalements résolus par un admin
     */
    @Query("SELECT mr FROM ModerationReport mr WHERE mr.resolvedBy = :adminId")
    Page<ModerationReport> findByResolvedBy(@Param("adminId") Long adminId, Pageable pageable);

    /**
     * Statistiques des signalements par mois
     */
    @Query("SELECT YEAR(mr.reportedAt) as year, MONTH(mr.reportedAt) as month, COUNT(mr) as count " +
           "FROM ModerationReport mr " +
           "WHERE mr.reportedAt >= :startDate " +
           "GROUP BY YEAR(mr.reportedAt), MONTH(mr.reportedAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getReportStatsByMonth(@Param("startDate") LocalDateTime startDate);

    /**
     * Trouve les signalements avec filtres avancés
     */
    @Query("SELECT mr FROM ModerationReport mr WHERE " +
           "(:status IS NULL OR mr.status = :status) AND " +
           "(:type IS NULL OR mr.type = :type) AND " +
           "(:priorityLevel IS NULL OR mr.priorityLevel = :priorityLevel) AND " +
           "(:reportedUserId IS NULL OR mr.reportedUser.id = :reportedUserId) AND " +
           "(:reporterId IS NULL OR mr.reporter.id = :reporterId) AND " +
           "(:startDate IS NULL OR mr.reportedAt >= :startDate) AND " +
           "(:endDate IS NULL OR mr.reportedAt <= :endDate)")
    Page<ModerationReport> findWithFilters(
        @Param("status") ReportStatus status,
        @Param("type") ReportType type,
        @Param("priorityLevel") Integer priorityLevel,
        @Param("reportedUserId") Long reportedUserId,
        @Param("reporterId") Long reporterId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Compte les signalements par statut pour les statistiques
     */
    @Query("SELECT mr.status, COUNT(mr) FROM ModerationReport mr GROUP BY mr.status")
    List<Object[]> countReportsByStatus();

    /**
     * Compte les signalements par type pour les statistiques
     */
    @Query("SELECT mr.type, COUNT(mr) FROM ModerationReport mr GROUP BY mr.type")
    List<Object[]> countReportsByType();

    /**
     * Trouve les signalements récents (dernières 24h)
     */
    @Query("SELECT mr FROM ModerationReport mr WHERE mr.reportedAt >= :yesterday ORDER BY mr.reportedAt DESC")
    List<ModerationReport> findRecentReports(@Param("yesterday") LocalDateTime yesterday);

    /**
     * Trouve les utilisateurs les plus signalés
     */
    @Query("SELECT mr.reportedUser.id, mr.reportedUser.email, COUNT(mr) as reportCount " +
           "FROM ModerationReport mr " +
           "WHERE mr.reportedAt >= :startDate " +
           "GROUP BY mr.reportedUser.id, mr.reportedUser.email " +
           "ORDER BY reportCount DESC")
    List<Object[]> findMostReportedUsers(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    /**
     * Trouve les signalements nécessitant une attention urgente
     */
    @Query("SELECT mr FROM ModerationReport mr WHERE " +
           "(mr.priorityLevel = 4) OR " +
           "(mr.priorityLevel >= 3 AND mr.reportedAt < :urgentCutoff) OR " +
           "(mr.status = 'PENDING' AND mr.reportedAt < :oldCutoff)")
    List<ModerationReport> findReportsNeedingUrgentAttention(
        @Param("urgentCutoff") LocalDateTime urgentCutoff,
        @Param("oldCutoff") LocalDateTime oldCutoff
    );

    /**
     * Recherche textuelle dans les signalements
     */
    @Query("SELECT mr FROM ModerationReport mr WHERE " +
           "LOWER(mr.reason) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mr.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mr.adminComment) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mr.reportedUser.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mr.reporter.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ModerationReport> searchReports(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Trouve les signalements en double (même rapporteur, même utilisateur signalé, même type)
     */
    @Query("SELECT mr FROM ModerationReport mr WHERE " +
           "mr.reporter.id = :reporterId AND " +
           "mr.reportedUser.id = :reportedUserId AND " +
           "mr.type = :type AND " +
           "mr.reportedAt >= :recentCutoff")
    List<ModerationReport> findDuplicateReports(
        @Param("reporterId") Long reporterId,
        @Param("reportedUserId") Long reportedUserId,
        @Param("type") ReportType type,
        @Param("recentCutoff") LocalDateTime recentCutoff
    );
}
