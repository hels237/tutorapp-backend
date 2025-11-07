package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.model.support.SecurityConfirmationToken;
import com.backend.tutor_app.repositories.SecurityConfirmationTokenRepository;
import com.backend.tutor_app.repositories.UserRepository;
import com.backend.tutor_app.services.EmailService;
import com.backend.tutor_app.services.SecurityConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityConfirmationServiceImpl implements SecurityConfirmationService {

    private final SecurityConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.security.confirmation.expiration-minutes:30}")
    private int expirationMinutes;

    @Value("${app.name:TutorApp}")
    private String appName;

    @Value("${app.support.email:support@tutorapp.com}")
    private String supportEmail;

    @Override
    @Transactional
    public SecurityConfirmationToken generateAndSendConfirmationToken(
            Utilisateur user, String reason, String ipAddress, String userAgent) {
        try {
            log.info("(PHASE 3 - Priorité 3)  Génération token de confirmation pour: {}", user.getEmail());
            invalidateAllUserConfirmationTokens(user.getId());
            
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);

            SecurityConfirmationToken confirmationToken = SecurityConfirmationToken.builder()
                    .utilisateur(user)
                    .token(token)
                    .expiresAt(expiresAt)
                    .isUsed(false)
                    .reason(reason)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            confirmationToken = confirmationTokenRepository.save(confirmationToken);
            log.info("(PHASE 3 - Priorité 3) ✅ Token créé, expire à: {}", expiresAt);
            
            sendConfirmationEmail(user, token, reason, ipAddress, userAgent);
            return confirmationToken;
        } catch (Exception e) {
            log.error("(PHASE 3 - Priorité 3) ❌ Erreur génération token: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du token de confirmation", e);
        }
    }

    @Override
    @Transactional
    public boolean confirmSecurityToken(String token, String confirmationIp, String confirmationUserAgent) {
        try {
            log.info("(PHASE 3 - Priorité 3) 🔍 Tentative de confirmation");
            
            SecurityConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Token de confirmation invalide"));

            if (!confirmationToken.isValid()) {
                if (confirmationToken.getIsUsed()) {
                    throw new RuntimeException("Ce lien de confirmation a déjà été utilisé");
                }
                if (confirmationToken.isExpired()) {
                    throw new RuntimeException("Ce lien de confirmation a expiré");
                }
            }

            Utilisateur user = confirmationToken.getUtilisateur();
            
            confirmationToken.markAsConfirmed(confirmationIp, confirmationUserAgent);
            confirmationTokenRepository.save(confirmationToken);
            
            user.setStatus(UserStatus.ACTIVE);
            user.setCompromised(false);
            user.setUnderSurveillance(false);
            userRepository.save(user);
            
            log.info("(PHASE 3 - Priorité 3) ✅ Compte {} débloqué avec succès", user.getEmail());
            return true;
        } catch (Exception e) {
            log.error("(PHASE 3 - Priorité 3) ❌ Erreur confirmation: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean hasPendingConfirmation(Long userId) {
        return confirmationTokenRepository.hasPendingConfirmation(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void invalidateAllUserConfirmationTokens(Long userId) {
        confirmationTokenRepository.invalidateAllUserTokens(userId);
        log.info("(PHASE 3 - Priorité 3) 🗑️ Tokens de confirmation invalidés pour user: {}", userId);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        confirmationTokenRepository.deleteExpiredTokens(cutoffDate);
        log.info("(PHASE 3 - Priorité 3) 🧹 Nettoyage des tokens expirés effectué");
    }

    private void sendConfirmationEmail(Utilisateur user, String token, String reason, 
                                      String ipAddress, String userAgent) {
        try {
            String confirmationUrl = frontendUrl + "/security/confirm?token=" + token;
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("user", user);
            variables.put("appName", appName);
            variables.put("reason", reason);
            variables.put("confirmationUrl", confirmationUrl);
            variables.put("expirationMinutes", expirationMinutes);
            variables.put("detectionTime", LocalDateTime.now());
            variables.put("ipAddress", ipAddress);
            variables.put("device", userAgent);
            variables.put("supportEmail", supportEmail);
            variables.put("frontendUrl", frontendUrl);

            emailService.sendTemplatedEmail(
                user.getEmail(),
                "🔐 Confirmation de Sécurité Requise - Action Immédiate",
                "email/security-confirmation",
                variables
            );
            
            log.info("(PHASE 3 - Priorité 3)  Email de confirmation envoyé à: {}", user.getEmail());
        } catch (Exception e) {
            log.error("(PHASE 3 - Priorité 3) ❌ Erreur envoi email confirmation: {}", e.getMessage());
        }
    }
}
