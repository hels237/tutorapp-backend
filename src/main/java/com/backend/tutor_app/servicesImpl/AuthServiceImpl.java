package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.AuthRequest;
import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.dto.Auth.RegisterRequest;
import com.backend.tutor_app.dto.Auth.ResetPasswordRequest;
import com.backend.tutor_app.model.User;
import com.backend.tutor_app.model.enums.Role;
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

            User user = (User) authentication.getPrincipal();
            
            // Vérifications supplémentaires
            validateUserForLogin(user);
            
            // Génération des tokens (TokenService délègue maintenant à JwtServiceUtil)
            String jwtToken = tokenService.generateJwtToken(user);  // ← Délègue à JwtServiceUtil.generateToken()
            String refreshToken = tokenService.createRefreshToken(user, request.getDeviceInfo(), clientIp).getToken();
            
            // Mise à jour des informations de connexion
            userService.updateLastLogin(user.getId(), LocalDateTime.now());
            userService.resetLoginAttempts(user.getId());
            
            // Enregistrement de la connexion réussie
            rateLimitService.recordSuccessfulLogin(clientIp, request.getEmail());
            
            log.info("Connexion réussie pour l'utilisateur: {}", user.getEmail());
            
            return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 heure
                .user(mapUserToDto(user))
                .build();
                
        } catch (AuthenticationException e) {
            // Enregistrement de l'échec de connexion
            rateLimitService.recordFailedLogin(clientIp, request.getEmail());
            
            // Incrémenter les tentatives de connexion si l'utilisateur existe
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
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
            User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : Role.STUDENT)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .loginAttempts(0)
                .passwordChangedAt(LocalDateTime.now())
                .build();

            User savedUser = userService.createUser(user);
            
            // Envoi de l'email de vérification
            sendEmailVerification(savedUser.getEmail());
            
            // Génération des tokens pour connexion automatique après inscription (délégation JwtServiceUtil)
            String jwtToken = tokenService.generateJwtToken(savedUser);  // ← Délègue à JwtServiceUtil.generateToken()
            String refreshToken = tokenService.createRefreshToken(savedUser, request.getDeviceInfo(), clientIp).getToken();
            
            // Enregistrement de la tentative d'inscription
            rateLimitService.recordRegistrationAttempt(clientIp);
            
            // Envoi de l'email de bienvenue
            emailService.sendWelcomeEmail(savedUser);
            
            log.info("Inscription réussie pour l'utilisateur: {}", savedUser.getEmail());
            
            return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(mapUserToDto(savedUser))
                .message("Inscription réussie. Veuillez vérifier votre email.")
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
            
            User user = refreshTokenEntity.getUser();
            
            // Vérification de l'état de l'utilisateur
            validateUserForLogin(user);
            
            // Génération d'un nouveau JWT
            String newJwtToken = tokenService.generateJwtToken(user);
            
            // Mise à jour de la date de dernière utilisation du refresh token
            tokenService.updateRefreshTokenLastUsed(refreshToken);
            
            log.info("Rafraîchissement de token réussi pour l'utilisateur: {}", user.getEmail());
            
            return AuthResponse.builder()
                .accessToken(newJwtToken)
                .refreshToken(refreshToken) // On garde le même refresh token
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(mapUserToDto(user))
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
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            if (user.getEmailVerified()) {
                throw new RuntimeException("Email déjà vérifié");
            }
            
            // Création du token de vérification
            String clientIp = getCurrentClientIp();
            var verificationToken = tokenService.createEmailVerificationToken(user, clientIp);
            
            // Envoi de l'email
            emailService.sendEmailVerification(user, verificationToken.getToken());
            
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
            
            User user = verificationToken.getUser();
            
            // Marquer l'email comme vérifié
            userService.markEmailAsVerified(user.getId());
            
            // Activer l'utilisateur si il était en attente de vérification
            if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
                userService.activateUser(user.getId());
            }
            
            // Marquer le token comme utilisé
            tokenService.markEmailVerificationTokenAsUsed(token);
            
            // Supprimer les autres tokens de vérification de cet utilisateur
            tokenService.deleteUserEmailVerificationTokens(user.getId());
            
            // Envoi de l'email de confirmation
            emailService.sendEmailVerificationConfirmation(user);
            
            log.info("Email vérifié avec succès pour l'utilisateur: {}", user.getEmail());
            
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
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Révocation des anciens tokens de réinitialisation
            tokenService.revokeAllUserPasswordResetTokens(user.getId());
            
            // Création du nouveau token
            var resetToken = tokenService.createPasswordResetToken(user);
            
            // Envoi de l'email
            emailService.sendPasswordResetEmail(user, resetToken.getToken());
            
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
            
            User user = resetToken.getUser();
            
            // Mise à jour du mot de passe
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
            user.setPasswordChangedAt(LocalDateTime.now());
            
            userRepository.save(user);
            
            // Marquer le token comme utilisé
            tokenService.markPasswordResetTokenAsUsed(request.getToken());
            
            // Révocation de tous les tokens de l'utilisateur pour forcer une nouvelle connexion
            revokeAllUserTokens(user.getId());
            
            // Remise à zéro des tentatives de connexion
            userService.resetLoginAttempts(user.getId());
            
            // Déverrouillage du compte si nécessaire
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                userService.unlockUser(user.getId());
            }
            
            // Envoi de l'email de confirmation
            emailService.sendPasswordChangeConfirmation(user);
            
            log.info("Mot de passe réinitialisé avec succès pour l'utilisateur: {}", user.getEmail());
            
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
    public User getCurrentUser(String token) {
        try {
            if (!tokenService.validateJwtToken(token)) {
                throw new RuntimeException("Token invalide");
            }
            
            Long userId = tokenService.getUserIdFromJwtToken(token);
            return userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
                
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
            User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Vérification du mot de passe actuel
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Mot de passe actuel incorrect");
            }
            
            // Mise à jour du mot de passe
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            user.setPasswordChangedAt(LocalDateTime.now());
            
            userRepository.save(user);
            
            // Révocation de tous les tokens pour forcer une nouvelle connexion
            revokeAllUserTokens(userId);
            
            // Envoi de l'email de confirmation
            emailService.sendPasswordChangeConfirmation(user);
            
            log.info("Mot de passe changé avec succès pour l'utilisateur ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors du changement de mot de passe pour l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors du changement de mot de passe");
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private void validateUserForLogin(User user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new RuntimeException("Compte suspendu. Contactez l'administration.");
        }
        
        if (user.getStatus() == UserStatus.DELETED) {
            throw new RuntimeException("Compte supprimé.");
        }
        
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Compte temporairement verrouillé. Réessayez plus tard.");
        }
    }

    private String getCurrentClientIp() {
        // TODO: Implémenter la récupération de l'IP client depuis la requête HTTP
        // Pour l'instant, on retourne une IP par défaut
        return "127.0.0.1";
    }

    private Object mapUserToDto(User user) {
        // TODO: Implémenter le mapping vers un DTO utilisateur
        // Pour l'instant, on retourne l'utilisateur directement (à éviter en production)
        return java.util.Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "role", user.getRole(),
            "status", user.getStatus(),
            "emailVerified", user.getEmailVerified()
        );
    }
}
