package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.model.User;
import com.backend.tutor_app.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Implémentation du service d'envoi d'emails pour TutorApp
 * Gère tous les types d'emails (vérification, réinitialisation, notifications)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@tutorapp.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.name:TutorApp}")
    private String appName;

    // Pattern pour valider les emails
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    @Override
    public void sendEmailVerification(User user, String verificationToken) {
        log.info("Envoi d'email de vérification à: {}", user.getEmail());
        
        try {
            String verificationUrl = generateEmailVerificationUrl(verificationToken);
            
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("appName", appName);
            
            String subject = "Vérifiez votre adresse email - " + appName;
            String templateName = "email-verification";
            
            sendTemplatedEmail(user.getEmail(), subject, templateName, 
                Map.of(
                    "user", user,
                    "verificationUrl", verificationUrl,
                    "appName", appName
                )
            );
            
            log.info("Email de vérification envoyé avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de vérification à: {} - {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification");
        }
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Envoi d'email de réinitialisation de mot de passe à: {}", user.getEmail());
        
        try {
            String resetUrl = generatePasswordResetUrl(resetToken);
            
            String subject = "Réinitialisation de votre mot de passe - " + appName;
            String templateName = "password-reset";
            
            sendTemplatedEmail(user.getEmail(), subject, templateName,
                Map.of(
                    "user", user,
                    "resetUrl", resetUrl,
                    "appName", appName,
                    "expirationTime", "1 heure"
                )
            );
            
            log.info("Email de réinitialisation envoyé avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation à: {} - {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation");
        }
    }

    @Override
    public void sendWelcomeEmail(User user) {
        log.info("Envoi d'email de bienvenue à: {}", user.getEmail());
        
        try {
            String subject = "Bienvenue sur " + appName + " !";
            String templateName = "welcome";
            
            sendTemplatedEmail(user.getEmail(), subject, templateName,
                Map.of(
                    "user", user,
                    "appName", appName,
                    "loginUrl", frontendUrl + "/login",
                    "supportEmail", fromEmail
                )
            );
            
            log.info("Email de bienvenue envoyé avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de bienvenue à: {} - {}", user.getEmail(), e.getMessage());
            // On ne lance pas d'exception pour ne pas bloquer l'inscription
        }
    }

    @Override
    public void sendEmailVerificationConfirmation(User user) {
        log.info("Envoi d'email de confirmation de vérification à: {}", user.getEmail());
        
        try {
            String subject = "Email vérifié avec succès - " + appName;
            String templateName = "email-verified";
            
            sendTemplatedEmail(user.getEmail(), subject, templateName,
                Map.of(
                    "user", user,
                    "appName", appName,
                    "dashboardUrl", frontendUrl + "/dashboard"
                )
            );
            
            log.info("Email de confirmation de vérification envoyé avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation de vérification à: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendPasswordChangeConfirmation(User user) {
        log.info("Envoi d'email de confirmation de changement de mot de passe à: {}", user.getEmail());
        
        try {
            String subject = "Mot de passe modifié - " + appName;
            String templateName = "password-changed";
            
            sendTemplatedEmail(user.getEmail(), subject, templateName,
                Map.of(
                    "user", user,
                    "appName", appName,
                    "changeTime", java.time.LocalDateTime.now(),
                    "supportEmail", fromEmail
                )
            );
            
            log.info("Email de confirmation de changement de mot de passe envoyé avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation de changement de mot de passe à: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendSecurityAlert(User user, String alertType, Map<String, Object> details) {
        log.info("Envoi d'alerte de sécurité à: {} - Type: {}", user.getEmail(), alertType);
        
        try {
            String subject = "Alerte de sécurité - " + appName;
            String templateName = "security-alert";
            
            Map<String, Object> templateVariables = Map.of(
                "user", user,
                "appName", appName,
                "alertType", alertType,
                "details", details,
                "alertTime", java.time.LocalDateTime.now(),
                "supportEmail", fromEmail
            );
            
            sendTemplatedEmail(user.getEmail(), subject, templateName, templateVariables);
            
            log.info("Alerte de sécurité envoyée avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'alerte de sécurité à: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendAccountSuspensionNotification(User user, String reason) {
        log.info("Envoi de notification de suspension de compte à: {}", user.getEmail());
        
        try {
            String subject = "Suspension de votre compte - " + appName;
            String templateName = "account-suspended";
            
            sendTemplatedEmail(user.getEmail(), subject, templateName,
                Map.of(
                    "user", user,
                    "appName", appName,
                    "reason", reason,
                    "supportEmail", fromEmail,
                    "appealUrl", frontendUrl + "/support/appeal"
                )
            );
            
            log.info("Notification de suspension envoyée avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification de suspension à: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendAccountReactivationNotification(User user) {
        log.info("Envoi de notification de réactivation de compte à: {}", user.getEmail());
        
        try {
            String subject = "Votre compte a été réactivé - " + appName;
            String templateName = "account-reactivated";
            
            sendTemplatedEmail(user.getEmail(), subject, templateName,
                Map.of(
                    "user", user,
                    "appName", appName,
                    "loginUrl", frontendUrl + "/login"
                )
            );
            
            log.info("Notification de réactivation envoyée avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification de réactivation à: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> templateVariables) {
        log.debug("Envoi d'email avec template: {} à: {}", templateName, to);
        
        try {
            if (!canSendEmail(to)) {
                log.warn("Envoi d'email bloqué pour: {}", to);
                return;
            }
            
            Context context = new Context();
            templateVariables.forEach(context::setVariable);
            
            String htmlContent;
            try {
                htmlContent = templateEngine.process(templateName, context);
            } catch (Exception e) {
                log.warn("Template {} non trouvé, envoi d'un email simple", templateName);
                // Fallback vers un email simple si le template n'existe pas
                String content = generateFallbackContent(templateVariables);
                sendSimpleEmail(to, subject, content);
                return;
            }
            
            sendHtmlEmail(to, subject, htmlContent);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email avec template {} à: {} - {}", templateName, to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email");
        }
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        log.debug("Envoi d'email simple à: {}", to);
        
        try {
            if (!canSendEmail(to)) {
                log.warn("Envoi d'email bloqué pour: {}", to);
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            
            log.debug("Email simple envoyé avec succès à: {}", to);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email simple à: {} - {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email");
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        log.debug("Envoi d'email HTML à: {}", to);
        
        try {
            if (!canSendEmail(to)) {
                log.warn("Envoi d'email bloqué pour: {}", to);
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            
            log.debug("Email HTML envoyé avec succès à: {}", to);
            
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email HTML à: {} - {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email HTML");
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String content, String attachmentPath, String attachmentName) {
        log.debug("Envoi d'email avec pièce jointe à: {}", to);
        
        try {
            if (!canSendEmail(to)) {
                log.warn("Envoi d'email bloqué pour: {}", to);
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content);
            
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(attachmentName, file);
            
            mailSender.send(message);
            
            log.debug("Email avec pièce jointe envoyé avec succès à: {}", to);
            
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email avec pièce jointe à: {} - {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email avec pièce jointe");
        }
    }

    @Override
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    @Override
    public boolean canSendEmail(String email) {
        // Vérification de base
        if (!isValidEmail(email)) {
            return false;
        }
        
        // TODO: Implémenter la vérification de blacklist
        // TODO: Implémenter la vérification de bounce/complaints
        
        return true;
    }

    @Override
    public String generateEmailVerificationUrl(String token) {
        return frontendUrl + "/verify-email?token=" + token;
    }

    @Override
    public String generatePasswordResetUrl(String token) {
        return frontendUrl + "/reset-password?token=" + token;
    }

    @Override
    public void sendTutorNotification(User user, String notificationType, Map<String, Object> data) {
        log.info("Envoi de notification tuteur à: {} - Type: {}", user.getEmail(), notificationType);
        
        try {
            String subject = getTutorNotificationSubject(notificationType);
            String templateName = "tutor-notification-" + notificationType.toLowerCase().replace("_", "-");
            
            Map<String, Object> templateVariables = Map.of(
                "user", user,
                "appName", appName,
                "notificationType", notificationType,
                "data", data,
                "dashboardUrl", frontendUrl + "/dashboard/tutor"
            );
            
            sendTemplatedEmail(user.getEmail(), subject, templateName, templateVariables);
            
            log.info("Notification tuteur envoyée avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification tuteur à: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendStudentNotification(User user, String notificationType, Map<String, Object> data) {
        log.info("Envoi de notification étudiant à: {} - Type: {}", user.getEmail(), notificationType);
        
        try {
            String subject = getStudentNotificationSubject(notificationType);
            String templateName = "student-notification-" + notificationType.toLowerCase().replace("_", "-");
            
            Map<String, Object> templateVariables = Map.of(
                "user", user,
                "appName", appName,
                "notificationType", notificationType,
                "data", data,
                "dashboardUrl", frontendUrl + "/dashboard/student"
            );
            
            sendTemplatedEmail(user.getEmail(), subject, templateName, templateVariables);
            
            log.info("Notification étudiant envoyée avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification étudiant à: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public void sendParentNotification(User user, String notificationType, Map<String, Object> data) {
        log.info("Envoi de notification parent à: {} - Type: {}", user.getEmail(), notificationType);
        
        try {
            String subject = getParentNotificationSubject(notificationType);
            String templateName = "parent-notification-" + notificationType.toLowerCase().replace("_", "-");
            
            Map<String, Object> templateVariables = Map.of(
                "user", user,
                "appName", appName,
                "notificationType", notificationType,
                "data", data,
                "dashboardUrl", frontendUrl + "/dashboard/parent"
            );
            
            sendTemplatedEmail(user.getEmail(), subject, templateName, templateVariables);
            
            log.info("Notification parent envoyée avec succès à: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification parent à: {} - {}", user.getEmail(), e.getMessage());
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private String generateFallbackContent(Map<String, Object> templateVariables) {
        StringBuilder content = new StringBuilder();
        content.append("Bonjour,\n\n");
        
        if (templateVariables.containsKey("user")) {
            User user = (User) templateVariables.get("user");
            content.append("Cher(e) ").append(user.getFirstName()).append(",\n\n");
        }
        
        content.append("Vous recevez cet email de la part de ").append(appName).append(".\n\n");
        content.append("Cordialement,\n");
        content.append("L'équipe ").append(appName);
        
        return content.toString();
    }

    private String getTutorNotificationSubject(String notificationType) {
        return switch (notificationType) {
            case "NEW_BOOKING" -> "Nouvelle réservation - " + appName;
            case "BOOKING_CANCELLED" -> "Réservation annulée - " + appName;
            case "PROFILE_APPROVED" -> "Profil approuvé - " + appName;
            case "DOCUMENT_REQUIRED" -> "Documents requis - " + appName;
            default -> "Notification - " + appName;
        };
    }

    private String getStudentNotificationSubject(String notificationType) {
        return switch (notificationType) {
            case "BOOKING_CONFIRMED" -> "Réservation confirmée - " + appName;
            case "LESSON_REMINDER" -> "Rappel de cours - " + appName;
            case "LESSON_COMPLETED" -> "Cours terminé - " + appName;
            case "TUTOR_ASSIGNED" -> "Tuteur assigné - " + appName;
            default -> "Notification - " + appName;
        };
    }

    private String getParentNotificationSubject(String notificationType) {
        return switch (notificationType) {
            case "CHILD_PROGRESS" -> "Progrès de votre enfant - " + appName;
            case "LESSON_REPORT" -> "Rapport de cours - " + appName;
            case "PAYMENT_DUE" -> "Paiement requis - " + appName;
            case "MONTHLY_SUMMARY" -> "Résumé mensuel - " + appName;
            default -> "Notification - " + appName;
        };
    }
}
