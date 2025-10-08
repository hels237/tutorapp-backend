package com.backend.tutor_app.services;

import com.backend.tutor_app.model.User;
import com.backend.tutor_app.model.support.EmailVerificationToken;
import com.backend.tutor_app.model.support.PasswordResetToken;
import com.backend.tutor_app.model.support.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des tokens pour TutorApp
 * Gère les tokens JWT, refresh tokens, tokens de vérification email et de réinitialisation
 */
public interface TokenService {
    
    // ==================== JWT TOKENS ====================
    
    /**
     * Génère un token JWT pour un utilisateur
     * @param user Utilisateur pour lequel générer le token
     * @return Token JWT
     */
    String generateJwtToken(User user);
    
    /**
     * Valide un token JWT
     * @param token Token JWT à valider
     * @return true si le token est valide
     */
    boolean validateJwtToken(String token);
    
    /**
     * Extrait l'email utilisateur d'un token JWT
     * @param token Token JWT
     * @return Email de l'utilisateur
     */
    String getEmailFromJwtToken(String token);
    
    /**
     * Extrait l'ID utilisateur d'un token JWT
     * @param token Token JWT
     * @return ID de l'utilisateur
     */
    Long getUserIdFromJwtToken(String token);
    
    /**
     * Vérifie si un token JWT est expiré
     * @param token Token JWT
     * @return true si le token est expiré
     */
    boolean isJwtTokenExpired(String token);
    
    // ==================== REFRESH TOKENS ====================
    
    /**
     * Crée un refresh token pour un utilisateur
     * @param user Utilisateur
     * @param deviceInfo Informations sur l'appareil
     * @param ipAddress Adresse IP
     * @return Refresh token créé
     */
    RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress);
    
    /**
     * Trouve un refresh token par sa valeur
     * @param token Valeur du token
     * @return Refresh token trouvé
     */
    Optional<RefreshToken> findRefreshToken(String token);
    
    /**
     * Valide un refresh token
     * @param token Refresh token à valider
     * @return true si le token est valide
     */
    boolean validateRefreshToken(String token);
    
    /**
     * Révoque un refresh token
     * @param token Token à révoquer
     */
    void revokeRefreshToken(String token);
    
    /**
     * Révoque tous les refresh tokens d'un utilisateur
     * @param userId ID de l'utilisateur
     */
    void revokeAllUserRefreshTokens(Long userId);
    
    /**
     * Met à jour la date de dernière utilisation d'un refresh token
     * @param token Token à mettre à jour
     */
    void updateRefreshTokenLastUsed(String token);
    
    /**
     * Nettoie les refresh tokens expirés
     * @return Nombre de tokens supprimés
     */
    int cleanupExpiredRefreshTokens();
    
    // ==================== EMAIL VERIFICATION TOKENS ====================
    
    /**
     * Crée un token de vérification d'email
     * @param user Utilisateur
     * @param ipAddress Adresse IP
     * @return Token de vérification créé
     */
    EmailVerificationToken createEmailVerificationToken(User user, String ipAddress);
    
    /**
     * Trouve un token de vérification d'email par sa valeur
     * @param token Valeur du token
     * @return Token de vérification trouvé
     */
    Optional<EmailVerificationToken> findEmailVerificationToken(String token);
    
    /**
     * Valide un token de vérification d'email
     * @param token Token à valider
     * @return true si le token est valide
     */
    boolean validateEmailVerificationToken(String token);
    
    /**
     * Marque un token de vérification d'email comme utilisé
     * @param token Token à marquer
     */
    void markEmailVerificationTokenAsUsed(String token);
    
    /**
     * Supprime tous les tokens de vérification d'email d'un utilisateur
     * @param userId ID de l'utilisateur
     */
    void deleteUserEmailVerificationTokens(Long userId);
    
    /**
     * Nettoie les tokens de vérification d'email expirés
     * @return Nombre de tokens supprimés
     */
    int cleanupExpiredEmailVerificationTokens();
    
    // ==================== PASSWORD RESET TOKENS ====================
    
    /**
     * Crée un token de réinitialisation de mot de passe
     * @param user Utilisateur
     * @return Token de réinitialisation créé
     */
    PasswordResetToken createPasswordResetToken(User user);
    
    /**
     * Trouve un token de réinitialisation de mot de passe par sa valeur
     * @param token Valeur du token
     * @return Token de réinitialisation trouvé
     */
    Optional<PasswordResetToken> findPasswordResetToken(String token);
    
    /**
     * Valide un token de réinitialisation de mot de passe
     * @param token Token à valider
     * @return true si le token est valide
     */
    boolean validatePasswordResetToken(String token);
    
    /**
     * Marque un token de réinitialisation comme utilisé
     * @param token Token à marquer
     */
    void markPasswordResetTokenAsUsed(String token);
    
    /**
     * Révoque tous les tokens de réinitialisation actifs d'un utilisateur
     * @param userId ID de l'utilisateur
     */
    void revokeAllUserPasswordResetTokens(Long userId);
    
    /**
     * Nettoie les tokens de réinitialisation expirés
     * @return Nombre de tokens supprimés
     */
    int cleanupExpiredPasswordResetTokens();
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Génère un token aléatoire sécurisé
     * @param length Longueur du token
     * @return Token généré
     */
    String generateSecureToken(int length);
    
    /**
     * Génère un token UUID
     * @return Token UUID
     */
    String generateUuidToken();
    
    /**
     * Vérifie si un utilisateur a trop de tokens actifs
     * @param userId ID de l'utilisateur
     * @param tokenType Type de token (refresh, email_verification, password_reset)
     * @return true si l'utilisateur a trop de tokens actifs
     */
    boolean hasUserTooManyActiveTokens(Long userId, String tokenType);
    
    /**
     * Récupère les statistiques des tokens
     * @return Map avec les statistiques
     */
    java.util.Map<String, Object> getTokenStatistics();
    
    /**
     * Nettoie tous les tokens expirés
     * @return Nombre total de tokens supprimés
     */
    int cleanupAllExpiredTokens();
}
