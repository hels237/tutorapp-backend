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
}
