package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.Auth.SecurityCheckResult;
import com.backend.tutor_app.model.Utilisateur;

/**
 * PHASE 4 - ÉTAPE 6 : Service de notifications de sécurité en temps réel
 * Gère l'envoi de notifications via WebSocket et FCM selon le contexte
 */
public interface SecurityNotificationService {
    
    /**
     * Envoie une notification de sécurité à l'utilisateur
     * Utilise WebSocket si connecté, sinon FCM
     * @param user Utilisateur concerné
     * @param checkResult Résultat de la vérification de sécurité
     */
    void sendSecurityNotification(Utilisateur user, SecurityCheckResult checkResult);
    
    /**
     * Envoie une notification WARNING dans l'app (WebSocket)
     * @param userId ID de l'utilisateur
     * @param title Titre de la notification
     * @param message Message de la notification
     * @param details Détails additionnels
     */
    void sendWarningNotification(Long userId, String title, String message, Object details);
    
    /**
     * Envoie une notification CRITICAL aux admins (WebSocket temps réel)
     * @param userId ID de l'utilisateur concerné
     * @param title Titre de l'alerte
     * @param message Message de l'alerte
     * @param details Détails de l'incident
     */
    void sendCriticalAdminNotification(Long userId, String title, String message, Object details);
    
    /**
     * Envoie une notification push FCM
     * @param userId ID de l'utilisateur
     * @param title Titre de la notification
     * @param body Corps de la notification
     * @param data Données additionnelles
     */
    void sendPushNotification(Long userId, String title, String body, Object data);
    
    /**
     * Vérifie si un utilisateur est connecté via WebSocket
     * @param userId ID de l'utilisateur
     * @return true si connecté
     */
    boolean isUserConnected(Long userId);
}
