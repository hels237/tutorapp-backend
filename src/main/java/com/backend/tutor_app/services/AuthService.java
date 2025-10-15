package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.Auth.AuthRequest;
import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.dto.Auth.RegisterRequest;
import com.backend.tutor_app.dto.Auth.ResetPasswordRequest;
import com.backend.tutor_app.dto.Auth.UserDto;
import com.backend.tutor_app.model.User;
import com.backend.tutor_app.model.enums.SocialProvider;

/**
 * Service d'authentification pour TutorApp
 * Gère l'inscription, connexion, validation email et social login
 */
public interface AuthService {
    
    /**
     * Authentifie un utilisateur avec email/mot de passe
     * @param request Données de connexion (email, password)
     * @return AuthResponse avec tokens JWT et informations utilisateur
     */
    AuthResponse login(AuthRequest request);
    
    /**
     * Inscrit un nouvel utilisateur
     * @param request Données d'inscription (email, password, firstName, lastName, role)
     * @return AuthResponse avec tokens JWT et informations utilisateur
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * Déconnecte un utilisateur et révoque ses tokens
     * @param token Token JWT à révoquer
     */
    void logout(String token);
    
    /**
     * Renouvelle un token JWT avec un refresh token
     * @param refreshToken Token de rafraîchissement
     * @return AuthResponse avec nouveaux tokens
     */
    AuthResponse refreshToken(String refreshToken);
    
    /**
     * Envoie un email de vérification à l'utilisateur
     * @param email Adresse email à vérifier
     */
    void sendEmailVerification(String email);
    
    /**
     * Vérifie l'email d'un utilisateur avec un token
     * @param token Token de vérification email
     */
    void verifyEmail(String token);
    
    /**
     * Envoie un email de réinitialisation de mot de passe
     * @param email Adresse email pour la réinitialisation
     */
    void sendPasswordReset(String email);
    
    /**
     * Réinitialise le mot de passe avec un token
     * @param request Données de réinitialisation (token, newPassword)
     */
    void resetPassword(ResetPasswordRequest request);
    
    /**
     * Authentifie un utilisateur via OAuth2 (Google, Facebook, GitHub)
     * @param provider Fournisseur OAuth2
     * @param code Code d'autorisation OAuth2
     * @return AuthResponse avec tokens JWT et informations utilisateur
     */
    AuthResponse socialLogin(SocialProvider provider, String code);
    
    /**
     * Vérifie si un utilisateur est authentifié et actif
     * @param token Token JWT à vérifier
     * @return true si le token est valide et l'utilisateur actif
     */
    boolean isAuthenticated(String token);
    
    /**
     * Valide un token JWT (alias isAuthenticated)
     */
    default boolean validateToken(String token) { return isAuthenticated(token); }
    
    /**
     * Récupère l'utilisateur actuel depuis un token JWT
     * @param token Token JWT
     * @return Utilisateur authentifié
     */
    UserDto getCurrentUser(String token);
    
    /**
     * Révoque tous les tokens d'un utilisateur
     * @param userId ID de l'utilisateur
     */
    void revokeAllUserTokens(Long userId);
    
    /**
     * Change le mot de passe d'un utilisateur authentifié
     * @param userId ID de l'utilisateur
     * @param currentPassword Mot de passe actuel
     * @param newPassword Nouveau mot de passe
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
}
