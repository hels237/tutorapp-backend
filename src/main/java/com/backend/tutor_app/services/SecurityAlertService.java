package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.Auth.SecurityCheckResult;
import com.backend.tutor_app.model.Utilisateur;

/**
 * (Q) PHASE 2 - ÉTAPE 2.3/2.4/2.5 : Service d'alertes sécurité
 * Gère l'envoi des alertes selon le niveau de risque détecté
 */
public interface SecurityAlertService {
    
    /**
     * (Q) PHASE 2 - Envoie les alertes appropriées selon le résultat de sécurité
     * @param user Utilisateur concerné
     * @param checkResult Résultat des vérifications de sécurité
     */
    void sendSecurityAlerts(Utilisateur user, SecurityCheckResult checkResult);
    
    /**
     * (Q) PHASE 2 - Envoie un email d'alerte de sécurité
     * @param user Utilisateur concerné
     * @param subject Sujet de l'email
     * @param message Message de l'email
     */
    void sendEmailAlert(Utilisateur user, String subject, String message);
    
    /**
     * (Q) PHASE 2 - Envoie un SMS d'alerte (si numéro disponible)
     * @param user Utilisateur concerné
     * @param message Message du SMS
     */
    void sendSmsAlert(Utilisateur user, String message);
    
    /**
     * (Q) PHASE 2 - Notifie les administrateurs d'une activité suspecte
     * @param user Utilisateur concerné
     * @param checkResult Résultat des vérifications
     */
    void notifyAdmins(Utilisateur user, SecurityCheckResult checkResult);
    
    /**
     * (Q) PHASE 2 - Marque un compte comme sous surveillance
     * @param userId ID de l'utilisateur
     */
    void markAccountUnderSurveillance(Long userId);
    
    /**
     * (PHASE 3 - Priorité 2) Marque un compte comme compromis
     * @param userId ID de l'utilisateur
     * @param reason Raison du compromis
     */
    void markAccountAsCompromised(Long userId, String reason);
    
    /**
     * (PHASE 3 - Priorité 3) Marque un compte comme compromis avec détails
     * @param userId ID de l'utilisateur
     * @param reason Raison du compromis
     * @param ipAddress IP de l'activité suspecte
     * @param userAgent User agent de l'activité suspecte
     */
    void markAccountAsCompromised(Long userId, String reason, String ipAddress, String userAgent);
    
    /**
     * (PHASE 3 - Priorité 2) Vérifie si un compte doit être automatiquement bloqué
     * @param checkResult Résultat des vérifications de sécurité
     * @return true si le compte doit être bloqué
     */
    boolean shouldBlockAccount(SecurityCheckResult checkResult);
}
