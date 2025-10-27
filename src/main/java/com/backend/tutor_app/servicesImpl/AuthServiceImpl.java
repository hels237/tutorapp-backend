package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.AuthRequest;
import com.backend.tutor_app.dto.Auth.DeviceInfoDto; // (Q) PHASE 1 - Import DeviceInfoDto
import com.backend.tutor_app.utils.UserAgentParser; // (Q) PHASE 1 - Import UserAgentParser
import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.dto.Auth.RegisterRequest;
import com.backend.tutor_app.dto.Auth.ResetPasswordRequest;
import com.backend.tutor_app.dto.Auth.UserDto;
import com.backend.tutor_app.dto.user.UserProfileDto;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.SocialProvider;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.repositories.UserRepository;
import com.backend.tutor_app.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implémentation du service d'authentification pour TutorApp
 * INTÉGRÉE avec l'architecture de sécurité existante (SecurityConfig, JwtServiceUtil)
 * Gère l'inscription, connexion, validation email et social login
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    // INTÉGRATION avec architecture de sécurité existante
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;              // ← Déjà configuré dans SecurityConfig
    private final AuthenticationManager authenticationManager;  // ← Déjà configuré dans SecurityConfig
    
    // Services métier (TokenService maintenant intégré avec JwtServiceUtil)
    private final TokenService tokenService;                   // ← Refactorisé pour déléguer à JwtServiceUtil
    private final EmailService emailService;
    private final SocialAuthService socialAuthService;
    private final RateLimitService rateLimitService;
    private final UserService userService;
    
    // (Q) PHASE 1 - ÉTAPE 1.2 : Parser User Agent pour métadonnées enrichies
    private final UserAgentParser userAgentParser;

    @Override
    public AuthResponse login(AuthRequest request) {
        log.info("Tentative de connexion pour l'email: {}", request.getEmail());
        
        // Vérification du rate limiting
        String clientIp = getCurrentClientIp();
        if (!rateLimitService.isLoginAllowed(clientIp, request.getEmail())) {
            log.warn("Tentative de connexion bloquée par rate limiting pour: {}", request.getEmail());
            throw new RuntimeException("Trop de tentatives de connexion. Veuillez réessayer plus tard.");
        }

        try {
            // Authentification avec Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
            
            // Vérifications supplémentaires
            validateUserForLogin(utilisateur);
            
            // (Q) PHASE 1 - ÉTAPE 1.2 : Collecte et parsing des métadonnées enrichies
            DeviceInfoDto deviceInfo = userAgentParser.parseUserAgent(
                request.getDeviceInfo() != null ? request.getDeviceInfo() : "Unknown",
                clientIp,
                request.getTimezone(),
                request.getBrowserLanguage()
            );
            
            log.debug("(Q) PHASE 1 - Device détecté: {} depuis {} ({})", 
                deviceInfo.getDeviceSummary(), 
                deviceInfo.getIpAddress(),
                deviceInfo.getTimezone());
            
            // Génération des tokens (TokenService délègue maintenant à JwtServiceUtil)
            String jwtToken = tokenService.generateJwtToken(utilisateur);  // ← Délègue à JwtServiceUtil.generateToken()
            
            // (Q) PHASE 1 - ÉTAPE 1.2 : Création du Refresh Token avec métadonnées enrichies
            String refreshToken = tokenService.createRefreshTokenWithEnrichedMetadata(utilisateur, deviceInfo).getToken();
            
            // Mise à jour des informations de connexion
            userService.updateLastLogin(utilisateur.getId(), LocalDateTime.now());
            userService.resetLoginAttempts(utilisateur.getId());
            
            // Enregistrement de la connexion réussie
            rateLimitService.recordSuccessfulLogin(clientIp, request.getEmail());
            
            log.info("Connexion réussie pour l'utilisateur: {}", utilisateur.getEmail());
            
            return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 heure
                .user(mapUserToDto(utilisateur))
                .build();
                
        } catch (AuthenticationException e) {
            // Enregistrement de l'échec de connexion
            rateLimitService.recordFailedLogin(clientIp, request.getEmail());
            
            // Incrémenter les tentatives de connexion si l'utilisateur existe
            Optional<Utilisateur> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                userService.incrementLoginAttempts(userOpt.get().getId());
                
                // Verrouillage temporaire après 5 tentatives
                if (userOpt.get().getLoginAttempts() >= 5) {
                    userService.lockUser(userOpt.get().getId(), LocalDateTime.now().plusMinutes(15));
                    emailService.sendSecurityAlert(userOpt.get(), "ACCOUNT_LOCKED", 
                        java.util.Map.of("reason", "Trop de tentatives de connexion", "lockDuration", "15 minutes"));
                }
            }
            
            log.warn("Échec de connexion pour l'email: {} - {}", request.getEmail(), e.getMessage());
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email: {}", request.getEmail());
        
        // Vérification du rate limiting
        String clientIp = getCurrentClientIp();
        if (!rateLimitService.isRegistrationAllowed(clientIp)) {
            log.warn("Tentative d'inscription bloquée par rate limiting pour IP: {}", clientIp);
            throw new RuntimeException("Trop de tentatives d'inscription. Veuillez réessayer plus tard.");
        }

        // Vérification si l'email existe déjà
        if (userService.existsByEmail(request.getEmail())) {
            log.warn("Tentative d'inscription avec un email déjà existant: {}", request.getEmail());
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        try {
            // Création de l'utilisateur
            Utilisateur utilisateur = Utilisateur.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getUserTypeAsRole()) // Convertit "student" -> STUDENT
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .loginAttempts(0)
                .passwordChangedAt(LocalDateTime.now())
                .acceptTerms(request.getAcceptTerms() != null ? request.getAcceptTerms() : false)
                .acceptMarketing(request.getAcceptMarketing() != null ? request.getAcceptMarketing() : false)
                .build();

            Utilisateur savedUtilisateur = userService.createUser(utilisateur);
            
            // Envoi de l'email de vérification
            sendEmailVerification(savedUtilisateur.getEmail());
            
            // (Q) PHASE 1 - ÉTAPE 1.2 : Collecte et parsing des métadonnées enrichies pour l'inscription
            DeviceInfoDto deviceInfo = userAgentParser.parseUserAgent(
                request.getDeviceInfo() != null ? request.getDeviceInfo() : "Unknown",
                clientIp,
                request.getTimezone(),
                request.getBrowserLanguage()
            );
            
            log.debug("(Q) PHASE 1 - Inscription depuis: {} ({})", 
                deviceInfo.getDeviceSummary(), 
                deviceInfo.getIpAddress());
            
            // Génération des tokens pour connexion automatique après inscription (délégation JwtServiceUtil)
            String jwtToken = tokenService.generateJwtToken(savedUtilisateur);  // ← Délègue à JwtServiceUtil.generateToken()
            
            // (Q) PHASE 1 - ÉTAPE 1.2 : Création du Refresh Token avec métadonnées enrichies
            String refreshToken = tokenService.createRefreshTokenWithEnrichedMetadata(savedUtilisateur, deviceInfo).getToken();
            
            // Enregistrement de la tentative d'inscription
            rateLimitService.recordRegistrationAttempt(clientIp);
            
            // Envoi de l'email de bienvenue
            emailService.sendWelcomeEmail(savedUtilisateur);
            
            log.info("Inscription réussie pour l'utilisateur: {}", savedUtilisateur.getEmail());
            
            return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(mapUserToDto(savedUtilisateur))
                .build();
                
        } catch (Exception e) {
            log.error("Erreur lors de l'inscription pour l'email: {} - {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        log.info("Déconnexion d'un utilisateur");
        
        try {
            // Validation du token
            if (!tokenService.validateJwtToken(token)) {
                throw new RuntimeException("Token invalide");
            }
            
            // Récupération de l'utilisateur depuis le token
            Long userId = tokenService.getUserIdFromJwtToken(token);
            
            // Révocation de tous les tokens de l'utilisateur
            revokeAllUserTokens(userId);
            
            log.info("Déconnexion réussie pour l'utilisateur ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la déconnexion");
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Tentative de rafraîchissement de token");
        
        try {
            // Validation du refresh token
            if (!tokenService.validateRefreshToken(refreshToken)) {
                throw new RuntimeException("Refresh token invalide ou expiré");
            }
            
            // Récupération du refresh token
            var refreshTokenEntity = tokenService.findRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token non trouvé"));
            
            Utilisateur utilisateur = refreshTokenEntity.getUtilisateur();
            
            // Vérification de l'état de l'utilisateur
            validateUserForLogin(utilisateur);
            
            // Génération d'un nouveau JWT
            String newJwtToken = tokenService.generateJwtToken(utilisateur);
            
            // Mise à jour de la date de dernière utilisation du refresh token
            tokenService.updateRefreshTokenLastUsed(refreshToken);
            
            log.info("Rafraîchissement de token réussi pour l'utilisateur: {}", utilisateur.getEmail());
            
            return AuthResponse.builder()
                .accessToken(newJwtToken)
                .refreshToken(refreshToken) // On garde le même refresh token
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(mapUserToDto(utilisateur))
                .build();
                
        } catch (Exception e) {
            log.error("Erreur lors du rafraîchissement de token: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du rafraîchissement de token");
        }
    }

    @Override
    public void sendEmailVerification(String email) {
        log.info("Envoi d'email de vérification pour: {}", email);
        
        // Vérification du rate limiting
        if (!rateLimitService.isEmailVerificationAllowed(email)) {
            log.warn("Envoi d'email de vérification bloqué par rate limiting pour: {}", email);
            throw new RuntimeException("Trop de demandes de vérification. Veuillez réessayer plus tard.");
        }
        
        try {
            Utilisateur utilisateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            if (utilisateur.getEmailVerified()) {
                throw new RuntimeException("Email déjà vérifié");
            }
            
            // Création du token de vérification
            String clientIp = getCurrentClientIp();
            var verificationToken = tokenService.createEmailVerificationToken(utilisateur, clientIp);
            
            // Envoi de l'email
            emailService.sendEmailVerification(utilisateur, verificationToken.getToken());
            
            // Enregistrement de l'envoi
            rateLimitService.recordEmailVerificationSent(email);
            
            log.info("Email de vérification envoyé avec succès pour: {}", email);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de vérification pour: {} - {}", email, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification");
        }
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Vérification d'email avec token");
        
        try {
            // Validation du token de vérification
            if (!tokenService.validateEmailVerificationToken(token)) {
                throw new RuntimeException("Token de vérification invalide ou expiré");
            }
            
            var verificationToken = tokenService.findEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token de vérification non trouvé"));
            
            Utilisateur utilisateur = verificationToken.getUtilisateur();
            
            // Marquer l'email comme vérifié
            userService.markEmailAsVerified(utilisateur.getId());
            
            // Activer l'utilisateur si il était en attente de vérification
            if (utilisateur.getStatus() == UserStatus.PENDING_VERIFICATION) {
                userService.activateUser(utilisateur.getId());
            }
            
            // Marquer le token comme utilisé
            tokenService.markEmailVerificationTokenAsUsed(token);
            
            // Supprimer les autres tokens de vérification de cet utilisateur
            tokenService.deleteUserEmailVerificationTokens(utilisateur.getId());
            
            // Envoi de l'email de confirmation
            emailService.sendEmailVerificationConfirmation(utilisateur);
            
            log.info("Email vérifié avec succès pour l'utilisateur: {}", utilisateur.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de la vérification d'email: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la vérification d'email");
        }
    }

    @Override
    public void sendPasswordReset(String email) {
        log.info("Demande de réinitialisation de mot de passe pour: {}", email);
        
        // Vérification du rate limiting
        String clientIp = getCurrentClientIp();
        if (!rateLimitService.isPasswordResetAllowed(clientIp, email)) {
            log.warn("Demande de réinitialisation bloquée par rate limiting pour: {}", email);
            throw new RuntimeException("Trop de demandes de réinitialisation. Veuillez réessayer plus tard.");
        }
        
        try {
            Utilisateur utilisateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Révocation des anciens tokens de réinitialisation
            tokenService.revokeAllUserPasswordResetTokens(utilisateur.getId());
            
            // Création du nouveau token
            var resetToken = tokenService.createPasswordResetToken(utilisateur);
            
            // Envoi de l'email
            emailService.sendPasswordResetEmail(utilisateur, resetToken.getToken());
            
            // Enregistrement de la tentative
            rateLimitService.recordPasswordResetAttempt(clientIp, email);
            
            log.info("Email de réinitialisation envoyé avec succès pour: {}", email);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation pour: {} - {}", email, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation");
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Réinitialisation de mot de passe avec token");
        
        try {
            // Validation du token
            if (!tokenService.validatePasswordResetToken(request.getToken())) {
                throw new RuntimeException("Token de réinitialisation invalide ou expiré");
            }
            
            var resetToken = tokenService.findPasswordResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token de réinitialisation non trouvé"));
            
            Utilisateur utilisateur = resetToken.getUtilisateur();
            
            // Mise à jour du mot de passe
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            utilisateur.setPassword(encodedPassword);
            utilisateur.setPasswordChangedAt(LocalDateTime.now());
            
            userRepository.save(utilisateur);
            
            // Marquer le token comme utilisé
            tokenService.markPasswordResetTokenAsUsed(request.getToken());
            
            // Révocation de tous les tokens de l'utilisateur pour forcer une nouvelle connexion
            revokeAllUserTokens(utilisateur.getId());
            
            // Remise à zéro des tentatives de connexion
            userService.resetLoginAttempts(utilisateur.getId());
            
            // Déverrouillage du compte si nécessaire
            if (utilisateur.getLockedUntil() != null && utilisateur.getLockedUntil().isAfter(LocalDateTime.now())) {
                userService.unlockUser(utilisateur.getId());
            }
            
            // Envoi de l'email de confirmation
            emailService.sendPasswordChangeConfirmation(utilisateur);
            
            log.info("Mot de passe réinitialisé avec succès pour l'utilisateur: {}", utilisateur.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation de mot de passe: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la réinitialisation de mot de passe");
        }
    }

    @Override
    public AuthResponse socialLogin(SocialProvider provider, String code) {
        log.info("Tentative de connexion sociale avec {}", provider);
        
        // Vérification du rate limiting
        String clientIp = getCurrentClientIp();
        if (!rateLimitService.isSocialAuthAllowed(clientIp, provider.name())) {
            log.warn("Connexion sociale bloquée par rate limiting pour provider: {}", provider);
            throw new RuntimeException("Trop de tentatives de connexion sociale. Veuillez réessayer plus tard.");
        }
        
        try {
            // Délégation à SocialAuthService
            AuthResponse response = socialAuthService.authenticateWithSocialProvider(provider, code);
            
            // Enregistrement de la tentative
            rateLimitService.recordSocialAuthAttempt(clientIp, provider.name());
            
            log.info("Connexion sociale réussie avec {}", provider);
            return response;
            
        } catch (Exception e) {
            log.error("Erreur lors de la connexion sociale avec {} - {}", provider, e.getMessage());
            throw new RuntimeException("Erreur lors de la connexion sociale");
        }
    }

    @Override
    public boolean isAuthenticated(String token) {
        try {
            return tokenService.validateJwtToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserDto getCurrentUser(String token) {
        try {
            if (!tokenService.validateJwtToken(token)) {
                throw new RuntimeException("Token invalide");
            }
            
            Long userId = tokenService.getUserIdFromJwtToken(token);
            Utilisateur utilisateur = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            return UserDto.fromEntity(utilisateur);
                
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur actuel: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération de l'utilisateur");
        }
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        try {
            tokenService.revokeAllUserRefreshTokens(userId);
            log.info("Tous les tokens révoqués pour l'utilisateur ID: {}", userId);
        } catch (Exception e) {
            log.error("Erreur lors de la révocation des tokens pour l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la révocation des tokens");
        }
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Changement de mot de passe pour l'utilisateur ID: {}", userId);
        
        try {
            Utilisateur utilisateur = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Vérification du mot de passe actuel
            if (!passwordEncoder.matches(currentPassword, utilisateur.getPassword())) {
                throw new RuntimeException("Mot de passe actuel incorrect");
            }
            
            // Mise à jour du mot de passe
            String encodedPassword = passwordEncoder.encode(newPassword);
            utilisateur.setPassword(encodedPassword);
            utilisateur.setPasswordChangedAt(LocalDateTime.now());
            
            userRepository.save(utilisateur);
            
            // Révocation de tous les tokens pour forcer une nouvelle connexion
            revokeAllUserTokens(userId);
            
            // Envoi de l'email de confirmation
            emailService.sendPasswordChangeConfirmation(utilisateur);
            
            log.info("Mot de passe changé avec succès pour l'utilisateur ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors du changement de mot de passe pour l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors du changement de mot de passe");
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private void validateUserForLogin(Utilisateur utilisateur) {
        if (utilisateur.getStatus() == UserStatus.SUSPENDED) {
            throw new RuntimeException("Compte suspendu. Contactez l'administration.");
        }
        
        if (utilisateur.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("Compte supprimé.");
        }
        
        if (utilisateur.getLockedUntil() != null && utilisateur.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Compte temporairement verrouillé. Réessayez plus tard.");
        }
    }

    private String getCurrentClientIp() {
        // TODO: Implémenter la récupération de l'IP client depuis la requête HTTP
        // Pour l'instant, on retourne une IP par défaut
        return "127.0.0.1";
    }

    /**
     * Convertit une entité User en UserProfileDto pour l'API
     */
    private UserProfileDto mapUserToDto(Utilisateur utilisateur) {
        return UserProfileDto.fromEntity(utilisateur);
    }
}
