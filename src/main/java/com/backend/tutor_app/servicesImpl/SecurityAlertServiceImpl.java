package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.SecurityCheckResult;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.SecurityRiskLevel;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.repositories.UserRepository;
import com.backend.tutor_app.services.EmailService;
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SecurityAlertServiceImpl implements SecurityAlertService {
    
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final SecurityConfirmationService confirmationService;
    
    @Override
    public void sendSecurityAlerts(Utilisateur user, SecurityCheckResult checkResult) {
        log.info("(PHASE 3)  Envoi des alertes sécurité pour: {} (Risque: {})",
            user.getEmail(), 
            checkResult.getRiskLevel());
        
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
            
            log.info("(PHASE 3)  Email d'alerte envoyé pour: {}", user.getEmail());
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
            
            log.info("(PHASE 3) ✅ Email d'alerte envoyé avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("(PHASE 3) ❌ Erreur envoi email d'alerte: {}", e.getMessage());
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
            log.info("(Q) PHASE 2 - Envoi SMS d'alerte à: {}", user.getPhoneNumber());
            
            // (Q) PHASE 2 - TODO: Intégrer un service SMS (Twilio, AWS SNS, etc.)
            log.warn("(Q) PHASE 2 - SMS ALERT - To: {}, Message: {}", 
                user.getPhoneNumber(), message);
            
        } catch (Exception e) {
            log.error("(Q) PHASE 2 - Erreur envoi SMS d'alerte: {}", e.getMessage());
        }
    }
    
    @Override
    public void notifyAdmins(Utilisateur user, SecurityCheckResult checkResult) {
        try {
            log.warn("(Q) PHASE 2 - NOTIFICATION ADMIN - Activité suspecte pour: {} ({})", 
                user.getEmail(), 
                checkResult.getMessage());
            
            // (Q) PHASE 2 - TODO: Implémenter notification admin
            // Options :
            // 1. Email aux admins
            // 2. Webhook vers un système de monitoring
            // 3. Notification dans un dashboard admin
            // 4. Log dans un système centralisé (ELK, Splunk, etc.)
            
        } catch (Exception e) {
            log.error("(Q) PHASE 2 - Erreur notification admin: {}", e.getMessage());
        }
    }
    
    @Override
    public void markAccountUnderSurveillance(Long userId) {
        try {
            log.warn("(PHASE 3 - Priorité 2)  Marquer le compte sous surveillance: {}", userId);
            
            Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));
            
            user.setUnderSurveillance(true);
            user.setSurveillanceStartedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("(PHASE 3 - Priorité 2) ✅ Compte {} mis sous surveillance", user.getEmail());
            
        } catch (Exception e) {
            log.error("(PHASE 3 - Priorité 2) ❌ Erreur marquage surveillance: {}", e.getMessage());
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
            log.error("(PHASE 3 - Priorité 2/3)  COMPTE COMPROMIS DÉTECTÉ: {} - Raison: {}", userId, reason);
            
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
            
            log.error("(PHASE 3 - Priorité 2/3)  Compte {} marqué comme COMPROMIS et bloqué", user.getEmail());
            
            // (PHASE 3 - Priorité 3) Générer et envoyer token de confirmation obligatoire
            try {
                confirmationService.generateAndSendConfirmationToken(
                    user, 
                    reason, 
                    ipAddress != null ? ipAddress : "Unknown",
                    userAgent != null ? userAgent : "Unknown"
                );
                log.info("(PHASE 3 - Priorité 3)  Token de confirmation envoyé à: {}", user.getEmail());
            } catch (Exception e) {
                log.error("(PHASE 3 - Priorité 3) ❌ Erreur envoi token confirmation: {}", e.getMessage());
            }
            
            // Envoyer notification critique
            sendCriticalCompromisedAlert(user, reason);
            
        } catch (Exception e) {
            log.error("(PHASE 3 - Priorité 2/3) ❌ Erreur marquage compte compromis: {}", e.getMessage());
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
