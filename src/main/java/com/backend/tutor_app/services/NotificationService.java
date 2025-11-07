package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.notification.NotificationDTO;
import com.backend.tutor_app.dto.notification.NotificationRequest;
import com.backend.tutor_app.model.enums.NotificationPriority;

import java.util.List;
import java.util.Map;

/**
 * Service global de messaging et notifications
 * Centralise toutes les communications en temps réel (WebSocket, FCM, Email)
 * 
 * Utilisé par :
 * - SecurityAlertService : Alertes de sécurité
 * - ReservationService : Notifications de réservations
 * - ChatService : Messages instantanés
 * - AdminService : Notifications administratives
 */
public interface NotificationService {
    
    // ==================== ENVOI DE NOTIFICATIONS ====================
    
    /**
     * Envoie une notification WebSocket à un utilisateur connecté
     * @param userId ID de l'utilisateur destinataire
     * @param notification Notification à envoyer
     * @return true si envoyé avec succès, false sinon
     */
    boolean sendWebSocketNotification(Long userId, NotificationDTO notification);
    
    /**
     * Envoie une notification push FCM (Firebase Cloud Messaging)
     * @param userId ID de l'utilisateur destinataire
     * @param notification Notification à envoyer
     * @return true si envoyé avec succès, false sinon
     */
    boolean sendFCMNotification(Long userId, NotificationDTO notification);
    
    /**
     * Envoie une notification à un utilisateur (WebSocket + FCM + Persistance)
     * Méthode principale recommandée pour l'envoi de notifications
     * @param userId ID de l'utilisateur destinataire
     * @param notification Notification à envoyer
     * @return Notification persistée avec ID
     */
    NotificationDTO sendToUser(Long userId, NotificationRequest notification);
    
    /**
     * Envoie une notification à tous les administrateurs
     * @param notification Notification à envoyer
     * @return Liste des notifications envoyées
     */
    List<NotificationDTO> sendToAdmins(NotificationRequest notification);
    
    /**
     * Envoie une notification à un rôle spécifique (TUTOR, STUDENT, PARENT)
     * @param role Rôle cible
     * @param notification Notification à envoyer
     * @return Nombre de notifications envoyées
     */
    int sendToRole(String role, NotificationRequest notification);
    
    /**
     * Broadcast : Envoie une notification à tous les utilisateurs connectés
     * @param notification Notification à envoyer
     * @return Nombre de notifications envoyées
     */
    int broadcast(NotificationRequest notification);
    
    // ==================== GESTION DES CONNEXIONS ====================
    
    /**
     * Vérifie si un utilisateur est connecté via WebSocket
     * @param userId ID de l'utilisateur
     * @return true si connecté, false sinon
     */
    boolean isUserConnected(Long userId);
    
    /**
     * Récupère la liste des utilisateurs actuellement connectés
     * @return Liste des IDs des utilisateurs connectés
     */
    List<Long> getConnectedUsers();
    
    /**
     * Enregistre une session WebSocket pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param sessionId ID de la session WebSocket
     */
    void registerUserSession(Long userId, String sessionId);
    
    /**
     * Désenregistre une session WebSocket
     * @param sessionId ID de la session WebSocket
     */
    void unregisterUserSession(String sessionId);
    
    // ==================== GESTION DES TOKENS FCM ====================
    
    /**
     * Enregistre un token FCM pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param fcmToken Token FCM du device
     */
    void registerFCMToken(Long userId, String fcmToken);
    
    /**
     * Supprime un token FCM
     * @param fcmToken Token FCM à supprimer
     */
    void removeFCMToken(String fcmToken);
    
    /**
     * Récupère tous les tokens FCM d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des tokens FCM actifs
     */
    List<String> getUserFCMTokens(Long userId);
    
    // ==================== RÉCUPÉRATION DES NOTIFICATIONS ====================
    
    /**
     * Récupère toutes les notifications d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param unreadOnly true pour récupérer uniquement les non lues
     * @return Liste des notifications
     */
    List<NotificationDTO> getUserNotifications(Long userId, boolean unreadOnly);
    
    /**
     * Récupère une notification par son ID
     * @param notificationId ID de la notification
     * @return Notification trouvée
     */
    NotificationDTO getNotificationById(Long notificationId);
    
    /**
     * Compte le nombre de notifications non lues d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications non lues
     */
    int getUnreadCount(Long userId);
    
    // ==================== MARQUAGE ET SUPPRESSION ====================
    
    /**
     * Marque une notification comme lue
     * @param notificationId ID de la notification
     * @param userId ID de l'utilisateur (pour vérification)
     */
    void markAsRead(Long notificationId, Long userId);
    
    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications marquées
     */
    int markAllAsRead(Long userId);
    
    /**
     * Supprime une notification
     * @param notificationId ID de la notification
     * @param userId ID de l'utilisateur (pour vérification)
     */
    void deleteNotification(Long notificationId, Long userId);
    
    /**
     * Supprime toutes les notifications lues d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications supprimées
     */
    int deleteReadNotifications(Long userId);
    
    // ==================== NOTIFICATIONS SPÉCIALISÉES ====================
    
    /**
     * Envoie une alerte de sécurité (priorité CRITICAL)
     * @param userId ID de l'utilisateur
     * @param title Titre de l'alerte
     * @param message Message de l'alerte
     * @param metadata Métadonnées additionnelles (IP, device, etc.)
     */
    void sendSecurityAlert(Long userId, String title, String message, Map<String, Object> metadata);
    
    /**
     * Envoie une notification de réservation
     * @param userId ID de l'utilisateur
     * @param reservationId ID de la réservation
     * @param type Type de notification (NEW_BOOKING, CONFIRMED, CANCELLED, etc.)
     * @param message Message personnalisé
     */
    //void sendReservationNotification(Long userId, Long reservationId, NotificationType type, String message);
    
    /**
     * Envoie une notification de message chat
     * @param userId ID de l'utilisateur destinataire
     * @param senderId ID de l'expéditeur
     * @param senderName Nom de l'expéditeur
     * @param messagePreview Aperçu du message
     */
    //void sendChatNotification(Long userId, Long senderId, String senderName, String messagePreview);
    
    /**
     * Envoie une notification système
     * @param userId ID de l'utilisateur
     * @param title Titre
     * @param message Message
     * @param priority Priorité
     */
    void sendSystemNotification(Long userId, String title, String message, NotificationPriority priority);
    
    // ==================== NETTOYAGE ====================
    
    /**
     * Supprime les anciennes notifications (> 30 jours)
     * Méthode appelée par un cron job
     * @return Nombre de notifications supprimées
     */
    int cleanupOldNotifications();
    
    /**
     * Supprime les tokens FCM expirés ou invalides
     * @return Nombre de tokens supprimés
     */
    int cleanupExpiredFCMTokens();
}
