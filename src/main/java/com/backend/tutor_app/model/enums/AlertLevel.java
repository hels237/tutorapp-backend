package com.backend.tutor_app.model.enums;

/**
 * PHASE 4 : Niveaux d'alerte pour le système de sécurité multi-niveaux
 * Correspond aux niveaux de logs et aux actions à prendre
 */
public enum AlertLevel {
    /**
     * Niveau 1 : Changement mineur
     * - IP change dans le même pays
     * - Mise à jour de navigateur
     * - Changement de timezone
     * Actions : Log INFO uniquement, aucune notification utilisateur
     */
    INFO,
    
    /**
     * Niveau 2 : Changement notable
     * - IP change de pays
     * - Device complètement différent
     * - Connexion depuis un nouvel appareil
     * Actions : Log WARNING, Email d'information, Notification dans l'app, Pas de blocage
     */
    WARNING,
    
    /**
     * Niveau 3 : Activité suspecte
     * - IP depuis un pays à risque
     * - VPN/Proxy détecté
     * - Tentative de réutilisation d'un token révoqué
     * Actions : Log ERROR, Email, Blocage temporaire, Demande de confirmation, Notification admin si compte sensible
     */
    ERROR,
    
    /**
     * Niveau 4 : Attaque confirmée
     * - Réutilisation d'un token révoqué
     * - Multiples tentatives suspectes
     * - Pattern d'attaque détecté
     * Actions : Log CRITICAL, Email, Révocation de tous les tokens, Blocage du compte, Notification admin immédiate
     */
    CRITICAL;
    
    /**
     * Convertit un SecurityRiskLevel en AlertLevel
     */
    public static AlertLevel fromSecurityRiskLevel(SecurityRiskLevel riskLevel) {
        if (riskLevel == null) {
            return INFO;
        }
        
        switch (riskLevel) {
            case LOW:
                return INFO;
            case MEDIUM:
                return WARNING;
            case HIGH:
                return ERROR;
            case CRITICAL:
                return CRITICAL;
            default:
                return INFO;
        }
    }
    
    /**
     * Vérifie si ce niveau nécessite une notification utilisateur
     */
    public boolean requiresUserNotification() {
        return this == WARNING || this == ERROR || this == CRITICAL;
    }
    
    /**
     * Vérifie si ce niveau nécessite une notification admin
     */
    public boolean requiresAdminNotification() {
        return this == CRITICAL;
    }
    
    /**
     * Vérifie si ce niveau nécessite un blocage
     */
    public boolean requiresBlocking() {
        return this == ERROR || this == CRITICAL;
    }
    
    /**
     * Retourne le nom du niveau pour les logs
     */
    public String getLogLevelName() {
        switch (this) {
            case INFO:
                return "INFO";
            case WARNING:
                return "WARNING";
            case ERROR:
                return "ERROR";
            case CRITICAL:
                return "CRITICAL";
            default:
                return "UNKNOWN";
        }
    }
}
