package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.support.EmailVerificationToken;
import com.backend.tutor_app.model.support.PasswordResetToken;
import com.backend.tutor_app.model.support.RefreshToken;
import com.backend.tutor_app.repositories.EmailVerificationTokenRepository;
import com.backend.tutor_app.repositories.PasswordResetTokenRepository;
import com.backend.tutor_app.repositories.RefreshTokenRepository;
import com.backend.tutor_app.security.JwtServiceUtil;
import com.backend.tutor_app.security.CustomUserService;
import com.backend.tutor_app.services.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implémentation du service de gestion des tokens pour TutorApp
 * INTÉGRÉ avec JwtServiceUtil existant pour éviter la duplication
 * Gère les refresh tokens, tokens de vérification email et de réinitialisation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TokenServiceImpl implements TokenService {

    // INTÉGRATION avec services de sécurité existants
    private final JwtServiceUtil jwtServiceUtil;
    private final CustomUserService customUserService;
    
    // Repositories pour les tokens métier
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.refresh-token.expiration:2592000}")
    private int refreshTokenExpirationInSeconds; // 30 jours

    @Value("${app.email-verification-token.expiration:86400}")
    private int emailVerificationTokenExpirationInSeconds; // 24 heures

    @Value("${app.password-reset-token.expiration:3600}")
    private int passwordResetTokenExpirationInSeconds; // 1 heure

    private final SecureRandom secureRandom = new SecureRandom();

    // ==================== JWT TOKENS (DÉLÉGATION vers JwtServiceUtil) ====================

    @Override
    public String generateJwtToken(Utilisateur utilisateur) {
        log.debug("Génération du token JWT pour l'utilisateur: {} (délégation vers JwtServiceUtil)", utilisateur.getEmail());
        
        try {
            // DÉLÉGATION vers le service JWT existant
            return jwtServiceUtil.generateToken(utilisateur);
                
        } catch (Exception e) {
            log.error("Erreur lors de la génération du token JWT pour l'utilisateur: {} - {}", utilisateur.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du token JWT");
        }
    }

    @Override
    public boolean validateJwtToken(String token) {
        try {
            // DÉLÉGATION vers le service JWT existant avec UserDetails
            String username = jwtServiceUtil.extractUsername(token);
            UserDetails userDetails = customUserService.loadUserByUsername(username);
            return jwtServiceUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            log.debug("Token JWT invalide: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getEmailFromJwtToken(String token) {
        try {
            // DÉLÉGATION vers le service JWT existant
            return jwtServiceUtil.extractUsername(token);
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction de l'email du token JWT: {}", e.getMessage());
            throw new RuntimeException("Token JWT invalide");
        }
    }

    @Override
    public Long getUserIdFromJwtToken(String token) {
        try {
            // ADAPTATION pour extraire l'ID utilisateur avec JwtServiceUtil
            return jwtServiceUtil.extractClaim(token, claims -> claims.get("id", Long.class));
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction de l'ID utilisateur du token JWT: {}", e.getMessage());
            throw new RuntimeException("Token JWT invalide");
        }
    }

    @Override
    public boolean isJwtTokenExpired(String token) {
        try {
            // DÉLÉGATION vers le service JWT existant
            return jwtServiceUtil.extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true; // Si on ne peut pas parser le token, on considère qu'il est expiré
        }
    }

    // ==================== REFRESH TOKENS ====================

    @Override
    public RefreshToken createRefreshToken(Utilisateur utilisateur, String deviceInfo, String ipAddress) {
        log.debug("Création d'un refresh token pour l'utilisateur: {}", utilisateur.getEmail());
        
        try {
            // Suppression des anciens refresh tokens expirés pour cet utilisateur
            cleanupExpiredRefreshTokensForUser(utilisateur.getId());
            
            RefreshToken refreshToken = RefreshToken.builder()
                .utilisateur(utilisateur)
                .token(generateUuidToken())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationInSeconds))
                .isRevoked(false)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .lastUsed(LocalDateTime.now())
                .build();

            return refreshTokenRepository.save(refreshToken);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du refresh token pour l'utilisateur: {} - {}", utilisateur.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de la création du refresh token");
        }
    }

    @Override
    public Optional<RefreshToken> findRefreshToken(String token) {
        try {
            return refreshTokenRepository.findByToken(token);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche du refresh token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean validateRefreshToken(String token) {
        try {
            return refreshTokenRepository.findActiveToken(token, LocalDateTime.now()).isPresent();
        } catch (Exception e) {
            log.error("Erreur lors de la validation du refresh token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void revokeRefreshToken(String token) {
        log.debug("Révocation du refresh token");
        
        try {
            refreshTokenRepository.revokeToken(token);
        } catch (Exception e) {
            log.error("Erreur lors de la révocation du refresh token: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la révocation du refresh token");
        }
    }

    @Override
    public void revokeAllUserRefreshTokens(Long userId) {
        log.debug("Révocation de tous les refresh tokens pour l'utilisateur ID: {}", userId);
        
        try {
            refreshTokenRepository.revokeAllUserTokens(
                refreshTokenRepository.findById(userId).orElseThrow().getUtilisateur()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la révocation des refresh tokens pour l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la révocation des refresh tokens");
        }
    }

    @Override
    public void updateRefreshTokenLastUsed(String token) {
        try {
            refreshTokenRepository.updateLastUsed(token, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la dernière utilisation du refresh token: {}", e.getMessage());
        }
    }

    @Override
    public int cleanupExpiredRefreshTokens() {
        log.info("Nettoyage des refresh tokens expirés");
        
        try {
            return refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des refresh tokens expirés: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== EMAIL VERIFICATION TOKENS ====================

    @Override
    public EmailVerificationToken createEmailVerificationToken(Utilisateur utilisateur, String ipAddress) {
        log.debug("Création d'un token de vérification email pour l'utilisateur: {}", utilisateur.getEmail());
        
        try {
            // Suppression des anciens tokens de vérification pour cet utilisateur
            deleteUserEmailVerificationTokens(utilisateur.getId());
            
            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .utilisateur(utilisateur)
                .token(generateSecureToken(32))
                .expiresAt(LocalDateTime.now().plusSeconds(emailVerificationTokenExpirationInSeconds))
                .ipAddress(ipAddress)
                .build();

            return emailVerificationTokenRepository.save(verificationToken);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du token de vérification email pour l'utilisateur: {} - {}", utilisateur.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de la création du token de vérification email");
        }
    }

    @Override
    public Optional<EmailVerificationToken> findEmailVerificationToken(String token) {
        try {
            return emailVerificationTokenRepository.findByToken(token);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche du token de vérification email: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean validateEmailVerificationToken(String token) {
        try {
            Optional<EmailVerificationToken> tokenOpt = emailVerificationTokenRepository.findByToken(token);
            
            if (tokenOpt.isEmpty()) {
                return false;
            }
            
            EmailVerificationToken verificationToken = tokenOpt.get();
            
            // Vérifier si le token n'est pas expiré et n'a pas été utilisé
            return verificationToken.getExpiresAt().isAfter(LocalDateTime.now()) 
                && verificationToken.getVerifiedAt() == null;
                
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token de vérification email: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void markEmailVerificationTokenAsUsed(String token) {
        log.debug("Marquage du token de vérification email comme utilisé");
        
        try {
            Optional<EmailVerificationToken> tokenOpt = emailVerificationTokenRepository.findByToken(token);
            
            if (tokenOpt.isPresent()) {
                EmailVerificationToken verificationToken = tokenOpt.get();
                verificationToken.setVerifiedAt(LocalDateTime.now());
                emailVerificationTokenRepository.save(verificationToken);
            }
            
        } catch (Exception e) {
            log.error("Erreur lors du marquage du token de vérification email comme utilisé: {}", e.getMessage());
        }
    }

    @Override
    public void deleteUserEmailVerificationTokens(Long userId) {
        try {
            // Implementation simplifiée - en production, il faudrait une méthode dans le repository
            List<EmailVerificationToken> userTokens = emailVerificationTokenRepository.findAll()
                .stream()
                .filter(token -> token.getUtilisateur().getId().equals(userId))
                .toList();
            
            emailVerificationTokenRepository.deleteAll(userTokens);
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression des tokens de vérification email pour l'utilisateur ID: {} - {}", userId, e.getMessage());
        }
    }

    @Override
    public int cleanupExpiredEmailVerificationTokens() {
        log.info("Nettoyage des tokens de vérification email expirés");
        
        try {
            emailVerificationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            return 1; // Retour simplifié
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des tokens de vérification email expirés: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== PASSWORD RESET TOKENS ====================

    @Override
    public PasswordResetToken createPasswordResetToken(Utilisateur utilisateur) {
        log.debug("Création d'un token de réinitialisation de mot de passe pour l'utilisateur: {}", utilisateur.getEmail());
        
        try {
            // Révocation des anciens tokens actifs
            revokeAllUserPasswordResetTokens(utilisateur.getId());
            
            PasswordResetToken resetToken = PasswordResetToken.builder()
                .utilisateur(utilisateur)
                .token(generateSecureToken(32))
                .expiresAt(LocalDateTime.now().plusSeconds(passwordResetTokenExpirationInSeconds))
                .build();

            return passwordResetTokenRepository.save(resetToken);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création du token de réinitialisation pour l'utilisateur: {} - {}", utilisateur.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de la création du token de réinitialisation");
        }
    }

    @Override
    public Optional<PasswordResetToken> findPasswordResetToken(String token) {
        try {
            return passwordResetTokenRepository.findByToken(token);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche du token de réinitialisation: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        try {
            return passwordResetTokenRepository.findActiveToken(token, LocalDateTime.now()).isPresent();
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token de réinitialisation: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void markPasswordResetTokenAsUsed(String token) {
        log.debug("Marquage du token de réinitialisation comme utilisé");
        
        try {
            passwordResetTokenRepository.markTokenAsUsed(token, LocalDateTime.now());
        } catch (Exception e) {
            log.error("Erreur lors du marquage du token de réinitialisation comme utilisé: {}", e.getMessage());
        }
    }

    @Override
    public void revokeAllUserPasswordResetTokens(Long userId) {
        try {
            // Implementation simplifiée
            List<PasswordResetToken> userTokens = passwordResetTokenRepository.findAll()
                .stream()
                .filter(token -> token.getUtilisateur().getId().equals(userId))
                .toList();
            
            for (PasswordResetToken token : userTokens) {
                if (token.getUsedAt() == null && token.getExpiresAt().isAfter(LocalDateTime.now())) {
                    token.setUsedAt(LocalDateTime.now());
                    passwordResetTokenRepository.save(token);
                }
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la révocation des tokens de réinitialisation pour l'utilisateur ID: {} - {}", userId, e.getMessage());
        }
    }

    @Override
    public int cleanupExpiredPasswordResetTokens() {
        log.info("Nettoyage des tokens de réinitialisation expirés");
        
        try {
            return passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des tokens de réinitialisation expirés: {}", e.getMessage());
            return 0;
        }
    }

    // ==================== UTILITY METHODS ====================

    @Override
    public String generateSecureToken(int length) {
        byte[] tokenBytes = new byte[length];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    @Override
    public String generateUuidToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean hasUserTooManyActiveTokens(Long userId, String tokenType) {
        try {
            switch (tokenType.toLowerCase()) {
                case "refresh":
                    long activeRefreshTokens = refreshTokenRepository.countActiveTokensByUser(
                        refreshTokenRepository.findById(userId).orElseThrow().getUtilisateur(),
                        LocalDateTime.now()
                    );
                    return activeRefreshTokens > 5; // Maximum 5 refresh tokens actifs
                    
                case "email_verification":
                    // Un seul token de vérification email actif à la fois
                    return false; // Géré par la suppression des anciens tokens
                    
                case "password_reset":
                    long activeResetTokens = passwordResetTokenRepository.countActiveTokensByUser(
                        passwordResetTokenRepository.findById(userId).orElseThrow().getUtilisateur(),
                        LocalDateTime.now()
                    );
                    return activeResetTokens > 3; // Maximum 3 tokens de reset actifs
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du nombre de tokens actifs pour l'utilisateur ID: {} - {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getTokenStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Statistiques des refresh tokens
            long totalRefreshTokens = refreshTokenRepository.count();
            long activeRefreshTokens = refreshTokenRepository.findByIsRevokedFalse().size();
            long revokedRefreshTokens = refreshTokenRepository.findByIsRevokedTrue().size();
            
            stats.put("refreshTokens", Map.of(
                "total", totalRefreshTokens,
                "active", activeRefreshTokens,
                "revoked", revokedRefreshTokens
            ));
            
            // Statistiques des tokens de vérification email
            long totalEmailTokens = emailVerificationTokenRepository.count();
            stats.put("emailVerificationTokens", Map.of(
                "total", totalEmailTokens
            ));
            
            // Statistiques des tokens de réinitialisation
            long totalResetTokens = passwordResetTokenRepository.count();
            stats.put("passwordResetTokens", Map.of(
                "total", totalResetTokens
            ));
            
            return stats;
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des tokens: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public int cleanupAllExpiredTokens() {
        log.info("Nettoyage de tous les tokens expirés");
        
        int totalCleaned = 0;
        totalCleaned += cleanupExpiredRefreshTokens();
        totalCleaned += cleanupExpiredEmailVerificationTokens();
        totalCleaned += cleanupExpiredPasswordResetTokens();
        
        log.info("Nettoyage terminé - {} tokens supprimés", totalCleaned);
        return totalCleaned;
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private void cleanupExpiredRefreshTokensForUser(Long userId) {
        try {
            // Implementation simplifiée
            List<RefreshToken> userTokens = refreshTokenRepository.findAll()
                .stream()
                .filter(token -> token.getUtilisateur().getId().equals(userId) &&
                               token.getExpiresAt().isBefore(LocalDateTime.now()))
                .toList();
            
            refreshTokenRepository.deleteAll(userTokens);
            
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des refresh tokens expirés pour l'utilisateur ID: {} - {}", userId, e.getMessage());
        }
    }
}
