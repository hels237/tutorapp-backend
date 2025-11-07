package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Notification;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.NotificationPriority;
import com.backend.tutor_app.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des notifications
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // ==================== RECHERCHE PAR UTILISATEUR ====================
    
    /**
     * Récupère toutes les notifications d'un utilisateur
     */
    List<Notification> findByUserOrderByCreatedAtDesc(Utilisateur user);
    
    /**
     * Récupère les notifications d'un utilisateur avec pagination
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(Utilisateur user, Pageable pageable);
    
    /**
     * Récupère les notifications non lues d'un utilisateur
     */
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(Utilisateur user);
    
    /**
     * Récupère les notifications lues d'un utilisateur
     */
    List<Notification> findByUserAndReadTrueOrderByCreatedAtDesc(Utilisateur user);
    
    /**
     * Récupère une notification par ID et utilisateur (pour vérification de propriété)
     */
    Optional<Notification> findByIdAndUser(Long id, Utilisateur user);
    
    // ==================== COMPTAGE ====================
    
    /**
     * Compte le nombre de notifications non lues d'un utilisateur
     */
    int countByUserAndReadFalse(Utilisateur user);
    
    /**
     * Compte le nombre total de notifications d'un utilisateur
     */
    int countByUser(Utilisateur user);
    
    /**
     * Compte les notifications par type pour un utilisateur
     */
    int countByUserAndType(Utilisateur user, NotificationType type);
    
    /**
     * Compte les notifications par priorité pour un utilisateur
     */
    int countByUserAndPriority(Utilisateur user, NotificationPriority priority);
    
    // ==================== RECHERCHE PAR TYPE ET PRIORITÉ ====================
    
    /**
     * Récupère les notifications d'un utilisateur par type
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(Utilisateur user, NotificationType type);
    
    /**
     * Récupère les notifications d'un utilisateur par priorité
     */
    List<Notification> findByUserAndPriorityOrderByCreatedAtDesc(Utilisateur user, NotificationPriority priority);
    
    /**
     * Récupère les notifications critiques non lues d'un utilisateur
     */
    List<Notification> findByUserAndPriorityAndReadFalseOrderByCreatedAtDesc(
        Utilisateur user, 
        NotificationPriority priority
    );
    
    // ==================== RECHERCHE PAR DATE ====================
    
    /**
     * Récupère les notifications créées après une certaine date
     */
    List<Notification> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(
        Utilisateur user, 
        LocalDateTime date
    );
    
    /**
     * Récupère les notifications récentes (dernières 24h)
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user " +
           "AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(
        @Param("user") Utilisateur user,
        @Param("since") LocalDateTime since
    );
    
    /**
     * Récupère les anciennes notifications (> 30 jours)
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt < :before")
    List<Notification> findOldNotifications(@Param("before") LocalDateTime before);
    
    // ==================== MARQUAGE EN MASSE ====================
    
    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt " +
           "WHERE n.user = :user AND n.read = false")
    int markAllAsReadForUser(
        @Param("user") Utilisateur user,
        @Param("readAt") LocalDateTime readAt
    );
    
    /**
     * Marque les notifications d'un type spécifique comme lues
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt " +
           "WHERE n.user = :user AND n.type = :type AND n.read = false")
    int markAsReadByType(
        @Param("user") Utilisateur user,
        @Param("type") NotificationType type,
        @Param("readAt") LocalDateTime readAt
    );
    
    // ==================== SUPPRESSION ====================
    
    /**
     * Supprime toutes les notifications lues d'un utilisateur
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.read = true")
    int deleteReadNotificationsForUser(@Param("user") Utilisateur user);
    
    /**
     * Supprime les anciennes notifications (> 30 jours)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :before")
    int deleteOldNotifications(@Param("before") LocalDateTime before);
    
    /**
     * Supprime toutes les notifications d'un utilisateur
     */
    void deleteByUser(Utilisateur user);
    
    /**
     * Supprime les notifications d'un type spécifique pour un utilisateur
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.type = :type")
    int deleteByUserAndType(
        @Param("user") Utilisateur user,
        @Param("type") NotificationType type
    );
    
    // ==================== STATISTIQUES ====================
    
    /**
     * Récupère les statistiques de notifications par type
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE n.user = :user GROUP BY n.type")
    List<Object[]> getNotificationStatsByType(@Param("user") Utilisateur user);
    
    /**
     * Récupère les statistiques de notifications par priorité
     */
    @Query("SELECT n.priority, COUNT(n) FROM Notification n WHERE n.user = :user GROUP BY n.priority")
    List<Object[]> getNotificationStatsByPriority(@Param("user") Utilisateur user);
    
    /**
     * Vérifie si un utilisateur a des notifications non lues
     */
    boolean existsByUserAndReadFalse(Utilisateur user);
}
