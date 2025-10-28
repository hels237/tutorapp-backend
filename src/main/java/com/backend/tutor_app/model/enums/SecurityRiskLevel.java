package com.backend.tutor_app.model.enums;

/**
 * (Q) PHASE 2 - ÉTAPE 2.4 : Niveaux de risque pour la vérification IP
 * Utilisé pour déterminer les actions à prendre selon le niveau de risque détecté
 */
public enum SecurityRiskLevel {
    /**
     * (Q) Risque faible - Même pays, IP normale
     * Actions : Permettre, logger uniquement
     */
    LOW,
    
    /**
     * (Q) Risque moyen - Pays différent, IP suspecte
     * Actions : Permettre mais alerter l'utilisateur par email
     */
    MEDIUM,
    
    /**
     * (Q) Risque élevé - Pays à risque, VPN/Proxy détecté
     * Actions : Bloquer temporairement, email + SMS, demander confirmation
     */
    HIGH,
    
    /**
     * (Q) Risque critique - Token révoqué réutilisé, pattern d'attaque
     * Actions : Bloquer, révoquer tous les tokens, alerte admin
     */
    CRITICAL
}
