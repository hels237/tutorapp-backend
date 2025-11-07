package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.SecurityCheckResult;
import com.backend.tutor_app.dto.notification.NotificationRequest;
import com.backend.tutor_app.model.enums.NotificationPriority;
import com.backend.tutor_app.model.enums.NotificationType;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.SecurityRiskLevel;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.repositories.UserRepository;
import com.backend.tutor_app.services.EmailService;
import com.backend.tutor_app.services.NotificationService;
import com.backend.tutor_app.services.SecurityAlertService;
import com.backend.tutor_app.services.SecurityConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * (Q) PHASE 2 - ÉTAPE 2.3/2.4/2.5 : Implémentation du service d'alertes sécurité
 * REFACTORISÉ : Utilise maintenant NotificationService pour les notifications temps réel
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SecurityAlertServiceImpl implements SecurityAlertService {
    
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final SecurityConfirmationService confirmationService;
    private final NotificationService notificationService;
    
    @Override
    public void sendSecurityAlerts(Utilisateur user, SecurityCheckResult checkResult) {
        // (PHASE 4) Calculer l'alertLevel si non défini
        if (checkResult.getAlertLevel() == null) {
            checkResult.updateAlertLevel();
        }
        
        // (PHASE 4) Log avec le bon niveau
        logSecurityAlert(user, checkResult);
        
        log.debug("[PHASE 4] Traitement alertes - User: {}, RiskLevel: {}, AlertLevel: {}",
            user.getEmail(), checkResult.getRiskLevel(), checkResult.getAlertLevel());
        
        // ✅ NOUVEAU : Notification temps réel à l'utilisateur via NotificationService
        sendSecurityNotificationToUser(user, checkResult);
        
        // (PHASE 3) Email d'alerte si requis
        if (checkResult.isRequireEmailAlert()) {
            String subject = buildEmailSubject(checkResult.getRiskLevel());
            String riskLevel = checkResult.getRiskLevel().name();
            
            // Construction des détails pour le template
            java.util.Map<String, Object> details = new java.util.HashMap<>();
            details.put("currentIp", checkResult.getCurrentIp());
            details.put("previousIp", checkResult.getPreviousIp());
            details.put("currentCountry", checkResult.getCurrentCountry());
            details.put("previousCountry", checkResult.getPreviousCountry());
            details.put("currentDevice", checkResult.getCurrentDevice());
            details.put("previousDevice", checkResult.getPreviousDevice());
            details.put("currentBrowser", checkResult.getCurrentBrowser());
            details.put("previousBrowser", checkResult.getPreviousBrowser());
            details.put("currentOs", checkResult.getCurrentOs());
            details.put("previousOs", checkResult.getPreviousOs());
            details.put("ipChanged", checkResult.isIpChanged());
            details.put("countryChanged", checkResult.isCountryChanged());
            details.put("deviceChanged", checkResult.isDeviceChanged());
            details.put("browserChanged", checkResult.isBrowserChanged());
            details.put("osChanged", checkResult.isOsChanged());
            details.put("vpnDetected", checkResult.isVpnDetected());
            details.put("proxyDetected", checkResult.isProxyDetected());
            details.put("changeDetails", true);
            details.put("changesSummary", checkResult.getChangesSummary());
            
            // Envoi de l'email avec le bon template
            emailService.sendSecurityAlertWithRiskLevel(user, subject, riskLevel, details);
            
            log.info("[PHASE 4] Email alerte envoyé - User: {}, RiskLevel: {}", user.getEmail(), riskLevel);
        }
        
        // (PHASE 3) SMS d'alerte si requis
        if (checkResult.isRequireSmsAlert() && user.getPhoneNumber() != null) {
            String smsMessage = buildSmsMessage(checkResult);
            sendSmsAlert(user, smsMessage);
        }
        
        // (PHASE 3) Notification admin si requis
        if (checkResult.isRequireAdminNotification()) {
            notifyAdmins(user, checkResult);
        }
        
        // (PHASE 3) Marquer le compte sous surveillance si risque critique
        if (checkResult.getRiskLevel() == SecurityRiskLevel.CRITICAL) {
            markAccountUnderSurveillance(user.getId());
        }
    }
    
    /**
     * ✅ NOUVEAU : Envoie une notification temps réel à l'utilisateur via NotificationService
     */
    private void sendSecurityNotificationToUser(Utilisateur user, SecurityCheckResult checkResult) {
        try {
            // Construire les métadonnées
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ip", checkResult.getCurrentIp());
            metadata.put("country", checkResult.getCurrentCountry());
            metadata.put("device", checkResult.getCurrentDevice());
            metadata.put("browser", checkResult.getCurrentBrowser());
            metadata.put("os", checkResult.getCurrentOs());
            metadata.put("riskLevel", checkResult.getRiskLevel().name());
            
            // Déterminer le type de notification selon le risque
            NotificationType notifType;
            NotificationPriority priority;
            String title;
            String message;
            
            switch (checkResult.getRiskLevel()) {
                case CRITICAL:
                    notifType = NotificationType.SECURITY_ALERT;
                    priority = NotificationPriority.CRITICAL;
                    title = "🚨 Alerte Sécurité Critique";
                    message = "Activité suspecte détectée sur votre compte. Tous vos tokens ont été révoqués par sécurité.";
                    break;
                    
                case HIGH:
                    notifType = NotificationType.SECURITY_ALERT;
                    priority = NotificationPriority.HIGH;
                    title = "⚠️ Alerte Sécurité";
                    message = "Connexion inhabituelle détectée depuis " + 
                             (checkResult.getCurrentCountry() != null ? checkResult.getCurrentCountry() : "un emplacement inconnu");
                    break;
                    
                case MEDIUM:
                    notifType = NotificationType.SECURITY_LOGIN;
                    priority = NotificationPriority.MEDIUM;
                    title = "ℹ️ Nouvelle Connexion";
                    message = "Une nouvelle connexion a été détectée sur votre compte.";
                    break;
                    
                default:
                    notifType = NotificationType.SECURITY_LOGIN;
                    priority = NotificationPriority.LOW;
                    title = "Connexion détectée";
                    message = "Connexion à votre compte.";
            }
            
            // Utiliser la méthode spécialisée de NotificationService
            notificationService.sendSecurityAlert(
                user.getId(),
                title,
                message,
                metadata
            );
            
            log.info("✅ Notification temps réel envoyée à l'utilisateur {} via NotificationService", user.getEmail());
            
        } catch (Exception e) {
            log.error("❌ Erreur envoi notification temps réel: {}", e.getMessage());
        }
    }
    
    @Override
    public void sendEmailAlert(Utilisateur user, String subject, String message) {
        try {
            log.info("(PHASE 3)  Envoi email d'alerte à: {}", user.getEmail());
            
            // (PHASE 3) Utiliser le service email avec template
            // Extraction du niveau de risque depuis le subject
            String riskLevel = extractRiskLevelFromSubject(subject);
            
            // Construction des détails pour le template
            Map<String, Object> details = new java.util.HashMap<>();
            details.put("message", message);
            
            // Envoi de l'email avec le bon template
            emailService.sendSecurityAlertWithRiskLevel(user, subject, riskLevel, details);
            
            log.info("[PHASE 4] Email alerte envoyé avec succès - User: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("[PHASE 4] Erreur envoi email alerte - Error: {}", e.getMessage());
        }
    }
    
    /**
     * (PHASE 3) Extrait le niveau de risque depuis le sujet de l'email
     */
    private String extractRiskLevelFromSubject(String subject) {
        if (subject == null) {
            return "MEDIUM";
        }
        
        String upperSubject = subject.toUpperCase();
        if (upperSubject.contains("CRITIQUE") || upperSubject.contains("CRITICAL")) {
            return "CRITICAL";
        } else if (upperSubject.contains("ÉLEVÉ") || upperSubject.contains("HIGH") || upperSubject.contains("ALERTE")) {
            return "HIGH";
        } else {
            return "MEDIUM";
        }
    }
    
    @Override
    public void sendSmsAlert(Utilisateur user, String message) {
        try {
            log.info("[PHASE 4] Envoi SMS alerte - Phone: {}", user.getPhoneNumber());
            
            // (Q) PHASE 2 - TODO: Intégrer un service SMS (Twilio, AWS SNS, etc.)
            log.warn("[PHASE 4] SMS alerte (TODO implémentation) - Phone: {}, Message: {}", 
                user.getPhoneNumber(), message);
            
        } catch (Exception e) {
            log.error("[PHASE 4] Erreur envoi SMS alerte - Error: {}", e.getMessage());
        }
    }
    
    @Override
    public void notifyAdmins(Utilisateur user, SecurityCheckResult checkResult) {
        try {
            log.error("[PHASE 4][CRITICAL] Notification admin - User: {}, RiskLevel: {}, Message: {}", 
                user.getEmail(), checkResult.getRiskLevel(), checkResult.getMessage());
            
            // ✅ REFACTORISÉ : Utilisation de NotificationService
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userId", user.getId());
            metadata.put("userEmail", user.getEmail());
            metadata.put("riskLevel", checkResult.getRiskLevel().name());
            metadata.put("currentIp", checkResult.getCurrentIp());
            metadata.put("currentCountry", checkResult.getCurrentCountry());
            metadata.put("currentDevice", checkResult.getCurrentDevice());
            metadata.put("changesSummary", checkResult.getChangesSummary());
            
            NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.ADMIN_ACTION_REQUIRED)
                .priority(NotificationPriority.CRITICAL)
                .title("🚨 Alerte Sécurité - Action requise")
                .message(String.format(
                    "Activité suspecte détectée pour l'utilisateur %s (%s). Niveau de risque: %s",
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    checkResult.getRiskLevel().name()
                ))
                .metadata(metadata)
                .actionUrl("/admin/security/users/" + user.getId())
                .actionLabel("Voir les détails")
                .iconUrl("/icons/security-alert.svg")
                .sendEmail(true)  // Email aux admins
                .sendPush(true)   // Notification push
                .sendWebSocket(true) // Notification temps réel
                .build();
            
            notificationService.sendToAdmins(request);
            
            log.info("✅ Notification admin envoyée via NotificationService");
            
        } catch (Exception e) {
            log.error("[PHASE 4] Erreur notification admin - Error: {}", e.getMessage());
        }
    }
    
    @Override
    public void markAccountUnderSurveillance(Long userId) {
        try {
            log.warn("[PHASE 4][WARNING] Mise sous surveillance - UserID: {}", userId);
            
            Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));
            
            user.setUnderSurveillance(true);
            user.setSurveillanceStartedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.warn("[PHASE 4][WARNING] Compte mis sous surveillance - User: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("[PHASE 4] Erreur marquage surveillance - Error: {}", e.getMessage());
        }
    }
    
    /**
     * (PHASE 3 - Priorité 2/3) Marque un compte comme compromis et applique les mesures de sécurité
     */
    public void markAccountAsCompromised(Long userId, String reason) {
        markAccountAsCompromised(userId, reason, null, null);
    }
    
    /**
     * (PHASE 3 - Priorité 3) Marque un compte comme compromis avec détails complets
     */
    public void markAccountAsCompromised(Long userId, String reason, String ipAddress, String userAgent) {
        try {
            log.error("[PHASE 4][CRITICAL] Compte compromis détecté - UserID: {}, Raison: {}, IP: {}", userId, reason, ipAddress);
            
            Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));
            
            // Marquer comme compromis
            user.setCompromised(true);
            user.setCompromisedAt(LocalDateTime.now());
            user.setCompromisedReason(reason);
            user.setStatus(UserStatus.COMPROMISED);
            
            // Mettre sous surveillance
            user.setUnderSurveillance(true);
            if (user.getSurveillanceStartedAt() == null) {
                user.setSurveillanceStartedAt(LocalDateTime.now());
            }
            
            userRepository.save(user);
            
            log.error("[PHASE 4][CRITICAL] Compte marqué COMPROMIS et bloqué - User: {}, Status: {}", user.getEmail(), user.getStatus());
            
            // (PHASE 3 - Priorité 3) Générer et envoyer token de confirmation obligatoire
            try {
                confirmationService.generateAndSendConfirmationToken(
                    user, 
                    reason, 
                    ipAddress != null ? ipAddress : "Unknown",
                    userAgent != null ? userAgent : "Unknown"
                );
                log.info("[PHASE 4] Token confirmation envoyé - User: {}", user.getEmail());
            } catch (Exception e) {
                log.error("[PHASE 4] Erreur envoi token confirmation - Error: {}", e.getMessage());
            }
            
            // Envoyer notification critique
            sendCriticalCompromisedAlert(user, reason);
            
        } catch (Exception e) {
            log.error("[PHASE 4][CRITICAL] Erreur marquage compte compromis - Error: {}", e.getMessage());
        }
    }
    
    /**
     * (PHASE 3 - Priorité 2) Envoie une alerte critique pour compte compromis
     */
    private void sendCriticalCompromisedAlert(Utilisateur user, String reason) {
        try {
            String subject = "ALERTE CRITIQUE - Votre compte a été compromis";
            
            Map<String, Object> details = new HashMap<>();
            details.put("message", "Votre compte a été automatiquement bloqué suite à la détection d'une activité suspecte.");
            details.put("compromisedReason", reason);
            details.put("compromisedAt", user.getCompromisedAt());
            details.put("actionTaken", "Compte bloqué automatiquement");
            
            emailService.sendSecurityAlertWithRiskLevel(user, subject, "CRITICAL", details);
            
            log.info("(PHASE 3 - Priorité 2)  Email d'alerte COMPROMIS envoyé à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("(PHASE 3 - Priorité 2) ❌ Erreur envoi alerte compromis: {}", e.getMessage());
        }
    }
    
    /**
     * (PHASE 3 - Priorité 2) Vérifie si un compte doit être automatiquement bloqué
     */
    public boolean shouldBlockAccount(SecurityCheckResult checkResult) {
        // Bloquer si risque CRITICAL
        if (checkResult.getRiskLevel() == com.backend.tutor_app.model.enums.SecurityRiskLevel.CRITICAL) {
            return true;
        }
        
        // Bloquer si plusieurs indicateurs suspects
        int suspiciousCount = 0;
        if (checkResult.isVpnDetected()) suspiciousCount++;
        if (checkResult.isProxyDetected()) suspiciousCount++;
        if (checkResult.isCountryChanged()) suspiciousCount++;
        if (checkResult.isDeviceChanged()) suspiciousCount++;
        
        return suspiciousCount >= 3; // 3+ indicateurs = blocage
    }
    
    /**
     * (Q) PHASE 2 - Construit le sujet de l'email selon le niveau de risque
     */
    private String buildEmailSubject(SecurityRiskLevel riskLevel) {
        switch (riskLevel) {
            case CRITICAL:
                return " ALERTE SÉCURITÉ CRITIQUE - Activité suspecte détectée";
            case HIGH:
                return " Alerte sécurité - Connexion inhabituelle";
            case MEDIUM:
                return "ℹ Notification sécurité - Nouvelle connexion";
            default:
                return "Notification sécurité";
        }
    }
    
    /**
     * (Q) PHASE 2 - Construit le message de l'email
     */
    private String buildEmailMessage(Utilisateur user, SecurityCheckResult checkResult) {
        StringBuilder message = new StringBuilder();
        
        message.append("Bonjour ").append(user.getFirstName()).append(",\n\n");
        
        switch (checkResult.getRiskLevel()) {
            case CRITICAL:
                message.append("Une activité suspecte a été détectée sur votre compte.\n\n");
                message.append("Par mesure de sécurité, tous vos tokens d'accès ont été révoqués.\n");
                message.append("Veuillez vous reconnecter et changer votre mot de passe immédiatement.\n\n");
                break;
                
            case HIGH:
                message.append(" Une connexion inhabituelle a été détectée sur votre compte.\n\n");
                break;
                
            case MEDIUM:
                message.append(" Une nouvelle connexion a été détectée sur votre compte.\n\n");
                break;
        }
        
        // (Q) PHASE 2 - Détails de la connexion
        message.append("Détails de la connexion :\n");
        
        if (checkResult.isIpChanged()) {
            message.append("- IP : ").append(checkResult.getCurrentIp());
            if (checkResult.getCurrentCountry() != null) {
                message.append(" (").append(checkResult.getCurrentCountry()).append(")");
            }
            message.append("\n");
        }
        
        if (checkResult.getDeviceChangeType() != null) {
            message.append("- Appareil : ").append(checkResult.getCurrentDevice()).append("\n");
        }
        
        message.append("\n");
        message.append("Si ce n'est pas vous, veuillez sécuriser votre compte immédiatement.\n\n");
        message.append("Cordialement,\n");
        message.append("L'équipe TutorApp");
        
        return message.toString();
    }
    
    /**
     * (PHASE 4) Log l'alerte avec le bon niveau selon AlertLevel
     */
    private void logSecurityAlert(Utilisateur user, SecurityCheckResult checkResult) {
        String logMessage = String.format(
            "[PHASE 4] Alerte sécurité - User: %s, AlertLevel: %s, RiskLevel: %s, Message: %s",
            user.getEmail(),
            checkResult.getAlertLevel(),
            checkResult.getRiskLevel(),
            checkResult.getMessage()
        );
        
        switch (checkResult.getAlertLevel()) {
            case INFO:
                log.info(logMessage);
                break;
            case WARNING:
                log.warn(logMessage);
                break;
            case ERROR:
                log.error(logMessage);
                break;
            case CRITICAL:
                log.error("[CRITICAL] " + logMessage);
                break;
            default:
                log.info(logMessage);
        }
    }
    
    /**
     * (Q) PHASE 2 - Construit le message SMS
     */
    private String buildSmsMessage(SecurityCheckResult checkResult) {
        if (checkResult.getRiskLevel() == SecurityRiskLevel.CRITICAL) {
            return "TutorApp: Activité suspecte détectée. Votre compte a été sécurisé. Reconnectez-vous immédiatement.";
        } else {
            return "TutorApp: Nouvelle connexion détectée sur votre compte. Si ce n'est pas vous, sécurisez votre compte.";
        }
    }
}
