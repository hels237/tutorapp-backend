package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.model.User;
import com.backend.tutor_app.model.enums.SocialProvider;
import com.backend.tutor_app.model.support.SocialAccount;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service d'authentification sociale pour TutorApp
 * Gère l'authentification OAuth2 avec Google, Facebook, GitHub
 */
public interface SocialAuthService {
    
    // ==================== OAUTH2 AUTHENTICATION ====================
    
    /**
     * Authentifie un utilisateur via OAuth2
     * @param provider Fournisseur OAuth2 (Google, Facebook, GitHub)
     * @param authorizationCode Code d'autorisation OAuth2
     * @return AuthResponse avec tokens JWT et informations utilisateur
     */
    AuthResponse authenticateWithSocialProvider(SocialProvider provider, String authorizationCode);
    
    /**
     * Génère l'URL d'autorisation OAuth2
     * @param provider Fournisseur OAuth2
     * @param redirectUri URI de redirection après autorisation
     * @return URL d'autorisation complète
     */
    String generateAuthorizationUrl(SocialProvider provider, String redirectUri);
    
    /**
     * Échange un code d'autorisation contre un token d'accès
     * @param provider Fournisseur OAuth2
     * @param authorizationCode Code d'autorisation
     * @param redirectUri URI de redirection
     * @return Token d'accès OAuth2
     */
    String exchangeCodeForAccessToken(SocialProvider provider, String authorizationCode, String redirectUri);
    
    /**
     * Récupère les informations utilisateur depuis le fournisseur OAuth2
     * @param provider Fournisseur OAuth2
     * @param accessToken Token d'accès
     * @return Informations utilisateur du fournisseur
     */
    Map<String, Object> getUserInfoFromProvider(SocialProvider provider, String accessToken);
    
    // ==================== SOCIAL ACCOUNT MANAGEMENT ====================
    
    /**
     * Crée ou met à jour un compte social
     * @param user Utilisateur associé
     * @param provider Fournisseur OAuth2
     * @param providerData Données du fournisseur
     * @param accessToken Token d'accès
     * @param refreshToken Token de rafraîchissement (optionnel)
     * @return Compte social créé ou mis à jour
     */
    SocialAccount createOrUpdateSocialAccount(User user, SocialProvider provider, Map<String, Object> providerData, 
                                            String accessToken, String refreshToken);
    
    /**
     * Trouve un compte social par provider et provider ID
     * @param provider Fournisseur OAuth2
     * @param providerId ID utilisateur chez le fournisseur
     * @return Compte social trouvé
     */
    Optional<SocialAccount> findSocialAccount(SocialProvider provider, String providerId);
    
    /**
     * Trouve tous les comptes sociaux d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des comptes sociaux
     */
    List<SocialAccount> getUserSocialAccounts(Long userId);
    
    /**
     * Lie un compte social à un utilisateur existant
     * @param userId ID de l'utilisateur
     * @param provider Fournisseur OAuth2
     * @param authorizationCode Code d'autorisation
     * @return Compte social lié
     */
    SocialAccount linkSocialAccount(Long userId, SocialProvider provider, String authorizationCode);
    
    /**
     * Délie un compte social d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param provider Fournisseur OAuth2
     */
    void unlinkSocialAccount(Long userId, SocialProvider provider);
    
    /**
     * Vérifie si un utilisateur a un compte social pour un provider
     * @param userId ID de l'utilisateur
     * @param provider Fournisseur OAuth2
     * @return true si l'utilisateur a un compte social pour ce provider
     */
    boolean hasUserSocialAccount(Long userId, SocialProvider provider);
    
    // ==================== TOKEN MANAGEMENT ====================
    
    /**
     * Rafraîchit le token d'accès d'un compte social
     * @param socialAccountId ID du compte social
     * @return Nouveau token d'accès
     */
    String refreshSocialAccessToken(Long socialAccountId);
    
    /**
     * Vérifie si le token d'accès d'un compte social est expiré
     * @param socialAccountId ID du compte social
     * @return true si le token est expiré
     */
    boolean isSocialAccessTokenExpired(Long socialAccountId);
    
    /**
     * Révoque l'accès à un compte social
     * @param socialAccountId ID du compte social
     */
    void revokeSocialAccess(Long socialAccountId);
    
    // ==================== USER CREATION ====================
    
    /**
     * Crée un nouvel utilisateur à partir des données d'un fournisseur social
     * @param provider Fournisseur OAuth2
     * @param providerData Données du fournisseur
     * @return Utilisateur créé
     */
    User createUserFromSocialProvider(SocialProvider provider, Map<String, Object> providerData);
    
    /**
     * Met à jour les informations utilisateur avec les données du fournisseur social
     * @param user Utilisateur à mettre à jour
     * @param provider Fournisseur OAuth2
     * @param providerData Données du fournisseur
     * @return Utilisateur mis à jour
     */
    User updateUserFromSocialProvider(User user, SocialProvider provider, Map<String, Object> providerData);
    
    // ==================== VALIDATION ====================
    
    /**
     * Valide les données reçues d'un fournisseur OAuth2
     * @param provider Fournisseur OAuth2
     * @param providerData Données à valider
     * @return true si les données sont valides
     */
    boolean validateProviderData(SocialProvider provider, Map<String, Object> providerData);
    
    /**
     * Vérifie si un fournisseur OAuth2 est supporté
     * @param provider Fournisseur à vérifier
     * @return true si le fournisseur est supporté
     */
    boolean isSupportedProvider(SocialProvider provider);
    
    // ==================== STATISTICS ====================
    
    /**
     * Récupère les statistiques des connexions sociales
     * @return Map avec les statistiques par fournisseur
     */
    Map<String, Object> getSocialAuthStatistics();
    
    /**
     * Récupère le nombre d'utilisateurs par fournisseur social
     * @return Map avec le nombre d'utilisateurs par fournisseur
     */
    Map<SocialProvider, Long> getUserCountByProvider();
    
    // ==================== SYNC ====================
    
    /**
     * Synchronise les informations d'un compte social avec le fournisseur
     * @param socialAccountId ID du compte social
     */
    void syncSocialAccountData(Long socialAccountId);
    
    /**
     * Synchronise tous les comptes sociaux d'un utilisateur
     * @param userId ID de l'utilisateur
     */
    void syncAllUserSocialAccounts(Long userId);
}
