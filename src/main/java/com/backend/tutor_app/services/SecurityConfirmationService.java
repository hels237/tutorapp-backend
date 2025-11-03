package com.backend.tutor_app.services;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.support.SecurityConfirmationToken;

/**
 * (PHASE 3 - Priorité 3) Service de gestion des confirmations de sécurité
 */
public interface SecurityConfirmationService {

    /**
     * Génère et envoie un token de confirmation de sécurité
     * 
     * @param user Utilisateur concerné
     * @param reason Raison de la demande de confirmation
     * @param ipAddress IP de l'activité suspecte
     * @param userAgent User agent de l'activité suspecte
     * @return Token généré
     */
    SecurityConfirmationToken generateAndSendConfirmationToken(
        Utilisateur user, 
        String reason,
        String ipAddress,
        String userAgent
    );

    /**
     * Valide et confirme un token de sécurité
     * 
     * @param token Token à valider
     * @param confirmationIp IP de confirmation
     * @param confirmationUserAgent User agent de confirmation
     * @return true si la confirmation est réussie
     */
    boolean confirmSecurityToken(String token, String confirmationIp, String confirmationUserAgent);

    /**
     * Vérifie si un utilisateur a une confirmation en attente
     * 
     * @param userId ID de l'utilisateur
     * @return true si une confirmation est en attente
     */
    boolean hasPendingConfirmation(Long userId);

    /**
     * Invalide tous les tokens de confirmation d'un utilisateur
     * 
     * @param userId ID de l'utilisateur
     */
    void invalidateAllUserConfirmationTokens(Long userId);

    /**
     * Nettoie les tokens expirés (tâche planifiée)
     */
    void cleanupExpiredTokens();
}
