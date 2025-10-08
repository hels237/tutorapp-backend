package com.backend.tutor_app.services;

import java.time.Duration;
import java.util.Map;

/**
 * Service de limitation de taux (Rate Limiting) pour TutorApp
 * Protège contre les abus et attaques par déni de service
 */
public interface RateLimitService {
    
    // ==================== GENERAL RATE LIMITING ====================
    
    /**
     * Vérifie si une action est autorisée pour une clé donnée
     * @param key Clé d'identification (IP, userId, etc.)
     * @param action Type d'action (login, register, email, etc.)
     * @return true si l'action est autorisée
     */
    boolean isAllowed(String key, String action);
    
    /**
     * Vérifie si une action est autorisée avec limite personnalisée
     * @param key Clé d'identification
     * @param action Type d'action
     * @param maxAttempts Nombre maximum de tentatives
     * @param timeWindow Fenêtre de temps
     * @return true si l'action est autorisée
     */
    boolean isAllowed(String key, String action, int maxAttempts, Duration timeWindow);
    
    /**
     * Enregistre une tentative d'action
     * @param key Clé d'identification
     * @param action Type d'action
     */
    void recordAttempt(String key, String action);
    
    /**
     * Récupère le nombre de tentatives restantes
     * @param key Clé d'identification
     * @param action Type d'action
     * @return Nombre de tentatives restantes
     */
    int getRemainingAttempts(String key, String action);
    
    /**
     * Récupère le temps restant avant réinitialisation
     * @param key Clé d'identification
     * @param action Type d'action
     * @return Durée restante en secondes
     */
    long getTimeUntilReset(String key, String action);
    
    // ==================== AUTHENTICATION RATE LIMITING ====================
    
    /**
     * Vérifie si les tentatives de connexion sont autorisées
     * @param ipAddress Adresse IP
     * @param email Email de l'utilisateur
     * @return true si la connexion est autorisée
     */
    boolean isLoginAllowed(String ipAddress, String email);
    
    /**
     * Enregistre une tentative de connexion échouée
     * @param ipAddress Adresse IP
     * @param email Email de l'utilisateur
     */
    void recordFailedLogin(String ipAddress, String email);
    
    /**
     * Enregistre une connexion réussie (reset des compteurs)
     * @param ipAddress Adresse IP
     * @param email Email de l'utilisateur
     */
    void recordSuccessfulLogin(String ipAddress, String email);
    
    /**
     * Vérifie si l'inscription est autorisée
     * @param ipAddress Adresse IP
     * @return true si l'inscription est autorisée
     */
    boolean isRegistrationAllowed(String ipAddress);
    
    /**
     * Enregistre une tentative d'inscription
     * @param ipAddress Adresse IP
     */
    void recordRegistrationAttempt(String ipAddress);
    
    /**
     * Vérifie si la réinitialisation de mot de passe est autorisée
     * @param ipAddress Adresse IP
     * @param email Email de l'utilisateur
     * @return true si la réinitialisation est autorisée
     */
    boolean isPasswordResetAllowed(String ipAddress, String email);
    
    /**
     * Enregistre une tentative de réinitialisation de mot de passe
     * @param ipAddress Adresse IP
     * @param email Email de l'utilisateur
     */
    void recordPasswordResetAttempt(String ipAddress, String email);
    
    // ==================== EMAIL RATE LIMITING ====================
    
    /**
     * Vérifie si l'envoi d'email de vérification est autorisé
     * @param email Email destinataire
     * @return true si l'envoi est autorisé
     */
    boolean isEmailVerificationAllowed(String email);
    
    /**
     * Enregistre un envoi d'email de vérification
     * @param email Email destinataire
     */
    void recordEmailVerificationSent(String email);
    
    /**
     * Vérifie si l'envoi d'email général est autorisé
     * @param email Email destinataire
     * @param emailType Type d'email
     * @return true si l'envoi est autorisé
     */
    boolean isEmailSendingAllowed(String email, String emailType);
    
    /**
     * Enregistre un envoi d'email
     * @param email Email destinataire
     * @param emailType Type d'email
     */
    void recordEmailSent(String email, String emailType);
    
    // ==================== API RATE LIMITING ====================
    
    /**
     * Vérifie si l'accès API est autorisé
     * @param apiKey Clé API
     * @param endpoint Endpoint API
     * @return true si l'accès est autorisé
     */
    boolean isApiAccessAllowed(String apiKey, String endpoint);
    
    /**
     * Enregistre un appel API
     * @param apiKey Clé API
     * @param endpoint Endpoint API
     */
    void recordApiCall(String apiKey, String endpoint);
    
    /**
     * Vérifie si l'accès par IP est autorisé
     * @param ipAddress Adresse IP
     * @param endpoint Endpoint
     * @return true si l'accès est autorisé
     */
    boolean isIpAccessAllowed(String ipAddress, String endpoint);
    
    /**
     * Enregistre un accès par IP
     * @param ipAddress Adresse IP
     * @param endpoint Endpoint
     */
    void recordIpAccess(String ipAddress, String endpoint);
    
    // ==================== SOCIAL AUTH RATE LIMITING ====================
    
    /**
     * Vérifie si l'authentification sociale est autorisée
     * @param ipAddress Adresse IP
     * @param provider Fournisseur OAuth2
     * @return true si l'authentification est autorisée
     */
    boolean isSocialAuthAllowed(String ipAddress, String provider);
    
    /**
     * Enregistre une tentative d'authentification sociale
     * @param ipAddress Adresse IP
     * @param provider Fournisseur OAuth2
     */
    void recordSocialAuthAttempt(String ipAddress, String provider);
    
    // ==================== BLACKLIST MANAGEMENT ====================
    
    /**
     * Ajoute une IP à la liste noire
     * @param ipAddress Adresse IP à bloquer
     * @param reason Raison du blocage
     * @param duration Durée du blocage
     */
    void blacklistIp(String ipAddress, String reason, Duration duration);
    
    /**
     * Supprime une IP de la liste noire
     * @param ipAddress Adresse IP à débloquer
     */
    void removeIpFromBlacklist(String ipAddress);
    
    /**
     * Vérifie si une IP est dans la liste noire
     * @param ipAddress Adresse IP à vérifier
     * @return true si l'IP est bloquée
     */
    boolean isIpBlacklisted(String ipAddress);
    
    /**
     * Ajoute un utilisateur à la liste noire
     * @param userId ID de l'utilisateur à bloquer
     * @param reason Raison du blocage
     * @param duration Durée du blocage
     */
    void blacklistUser(Long userId, String reason, Duration duration);
    
    /**
     * Vérifie si un utilisateur est dans la liste noire
     * @param userId ID de l'utilisateur
     * @return true si l'utilisateur est bloqué
     */
    boolean isUserBlacklisted(Long userId);
    
    // ==================== WHITELIST MANAGEMENT ====================
    
    /**
     * Ajoute une IP à la liste blanche
     * @param ipAddress Adresse IP à autoriser
     * @param reason Raison de l'autorisation
     */
    void whitelistIp(String ipAddress, String reason);
    
    /**
     * Vérifie si une IP est dans la liste blanche
     * @param ipAddress Adresse IP à vérifier
     * @return true si l'IP est autorisée
     */
    boolean isIpWhitelisted(String ipAddress);
    
    // ==================== STATISTICS AND MONITORING ====================
    
    /**
     * Récupère les statistiques de rate limiting
     * @return Map avec les statistiques
     */
    Map<String, Object> getRateLimitStatistics();
    
    /**
     * Récupère les tentatives par IP
     * @param ipAddress Adresse IP
     * @return Map avec les tentatives par action
     */
    Map<String, Integer> getAttemptsByIp(String ipAddress);
    
    /**
     * Récupère les IPs les plus actives
     * @param limit Nombre d'IPs à retourner
     * @return Liste des IPs les plus actives
     */
    java.util.List<String> getMostActiveIps(int limit);
    
    /**
     * Récupère les actions les plus limitées
     * @param limit Nombre d'actions à retourner
     * @return Map des actions les plus limitées
     */
    Map<String, Long> getMostLimitedActions(int limit);
    
    // ==================== CONFIGURATION ====================
    
    /**
     * Met à jour la configuration de rate limiting pour une action
     * @param action Type d'action
     * @param maxAttempts Nombre maximum de tentatives
     * @param timeWindow Fenêtre de temps
     */
    void updateRateLimitConfig(String action, int maxAttempts, Duration timeWindow);
    
    /**
     * Récupère la configuration de rate limiting pour une action
     * @param action Type d'action
     * @return Configuration de l'action
     */
    Map<String, Object> getRateLimitConfig(String action);
    
    // ==================== CLEANUP ====================
    
    /**
     * Nettoie les entrées expirées du cache de rate limiting
     * @return Nombre d'entrées supprimées
     */
    int cleanupExpiredEntries();
    
    /**
     * Remet à zéro les compteurs pour une clé et une action
     * @param key Clé d'identification
     * @param action Type d'action
     */
    void resetCounters(String key, String action);
    
    /**
     * Remet à zéro tous les compteurs pour une clé
     * @param key Clé d'identification
     */
    void resetAllCounters(String key);
}
