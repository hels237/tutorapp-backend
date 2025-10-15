package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.TutorApplication;
import com.backend.tutor_app.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des candidatures de tuteurs
 */
@Repository
public interface TutorApplicationRepository extends JpaRepository<TutorApplication, Long> {

    /**
     * Trouve les candidatures par statut
     */
    List<TutorApplication> findByStatus(ApplicationStatus status);

    /**
     * Trouve les candidatures par statut avec pagination
     */
    Page<TutorApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * Trouve une candidature par ID utilisateur
     */
    Optional<TutorApplication> findByUserId(Long userId);

    /**
     * Vérifie si un utilisateur a une candidature avec un statut donné
     */
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);

    /**
     * Compte les candidatures par statut
     */
    @Query("SELECT COUNT(ta) FROM TutorApplication ta WHERE ta.status = :status")
    long countByStatus(@Param("status") ApplicationStatus status);

    /**
     * Trouve les candidatures soumises après une date donnée
     */
    @Query("SELECT ta FROM TutorApplication ta WHERE ta.submittedAt >= :startDate")
    List<TutorApplication> findApplicationsAfter(@Param("startDate") LocalDateTime startDate);

    /**
     * Trouve les candidatures en attente depuis plus de X jours
     */
    @Query("SELECT ta FROM TutorApplication ta WHERE ta.status = 'PENDING' AND ta.submittedAt < :cutoffDate")
    List<TutorApplication> findPendingApplicationsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Trouve les candidatures par utilisateur avec statuts multiples
     */
    @Query("SELECT ta FROM TutorApplication ta WHERE ta.user.id = :userId AND ta.status IN :statuses")
    List<TutorApplication> findByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<ApplicationStatus> statuses);

    /**
     * Trouve les candidatures par reviewer (admin)
     */
    @Query("SELECT ta FROM TutorApplication ta WHERE ta.reviewedBy = :adminId")
    Page<TutorApplication> findByReviewedBy(@Param("adminId") Long adminId, Pageable pageable);

    /**
     * Statistiques des candidatures par mois
     */
    @Query("SELECT YEAR(ta.submittedAt) as year, MONTH(ta.submittedAt) as month, COUNT(ta) as count " +
           "FROM TutorApplication ta " +
           "WHERE ta.submittedAt >= :startDate " +
           "GROUP BY YEAR(ta.submittedAt), MONTH(ta.submittedAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getApplicationStatsByMonth(@Param("startDate") LocalDateTime startDate);

    /**
     * Trouve les candidatures avec filtres avancés
     */
    @Query("SELECT ta FROM TutorApplication ta WHERE " +
           "(:status IS NULL OR ta.status = :status) AND " +
           "(:hasExperience IS NULL OR ta.hasTeachingExperience = :hasExperience) AND " +
           "(:hasCertifications IS NULL OR ta.hasCertifications = :hasCertifications) AND " +
           "(:minRate IS NULL OR ta.hourlyRate >= :minRate) AND " +
           "(:maxRate IS NULL OR ta.hourlyRate <= :maxRate) AND " +
           "(:startDate IS NULL OR ta.submittedAt >= :startDate) AND " +
           "(:endDate IS NULL OR ta.submittedAt <= :endDate)")
    Page<TutorApplication> findWithFilters(
        @Param("status") ApplicationStatus status,
        @Param("hasExperience") Boolean hasExperience,
        @Param("hasCertifications") Boolean hasCertifications,
        @Param("minRate") Double minRate,
        @Param("maxRate") Double maxRate,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Trouve les candidatures par matière enseignée
     */
    @Query("SELECT ta FROM TutorApplication ta JOIN ta.subjects s WHERE s = :subject")
    List<TutorApplication> findBySubject(@Param("subject") String subject);

    /**
     * Trouve les candidatures par niveau enseigné
     */
    @Query("SELECT ta FROM TutorApplication ta JOIN ta.levels l WHERE l = :level")
    List<TutorApplication> findByLevel(@Param("level") String level);

    /**
     * Compte les candidatures par statut pour les statistiques admin
     */
    @Query("SELECT ta.status, COUNT(ta) FROM TutorApplication ta GROUP BY ta.status")
    List<Object[]> countApplicationsByStatus();

    /**
     * Trouve les candidatures récentes (dernières 24h)
     */
    @Query("SELECT ta FROM TutorApplication ta WHERE ta.submittedAt >= :yesterday ORDER BY ta.submittedAt DESC")
    List<TutorApplication> findRecentApplications(@Param("yesterday") LocalDateTime yesterday);

    /**
     * Trouve les candidatures nécessitant une attention (en attente depuis > 7 jours)
     */
    @Query("SELECT ta FROM TutorApplication ta WHERE ta.status = 'PENDING' AND ta.submittedAt < :weekAgo")
    List<TutorApplication> findApplicationsNeedingAttention(@Param("weekAgo") LocalDateTime weekAgo);

    /**
     * Recherche textuelle dans les candidatures
     */
    @Query("SELECT ta FROM TutorApplication ta WHERE " +
           "LOWER(ta.motivation) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ta.experience) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ta.user.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ta.user.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ta.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<TutorApplication> searchApplications(@Param("searchTerm") String searchTerm, Pageable pageable);
}
