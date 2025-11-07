package com.backend.tutor_app.services;

/**
 * PHASE 4 : Service de détection de patterns d'attaque
 * Détecte les comportements suspects répétitifs
 */
public interface AttackPatternDetectionService {
    
    /**
     * Enregistre une tentative suspecte pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param reason Raison de la tentative suspecte
     */
    void recordSuspiciousAttempt(Long userId, String reason);
    
    /**
     * Vérifie si un utilisateur a un pattern d'attaque
     * Pattern détecté si 3+ tentatives suspectes dans les 15 dernières minutes
     * @param userId ID de l'utilisateur
     * @return true si pattern d'attaque détecté
     */
    boolean hasAttackPattern(Long userId);
    
    /**
     * Obtient le nombre de tentatives suspectes récentes
     * @param userId ID de l'utilisateur
     * @return Nombre de tentatives dans les 15 dernières minutes
     */
    int getRecentSuspiciousAttempts(Long userId);
    
    /**
     * Réinitialise le compteur de tentatives pour un utilisateur
     * @param userId ID de l'utilisateur
     */
    void resetAttempts(Long userId);
}
