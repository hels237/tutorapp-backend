package com.backend.tutor_app.services;

import com.backend.tutor_app.model.Utilisateur;

import java.util.Map;

/**
 * Service d'envoi d'emails pour TutorApp
 * Gère tous les types d'emails (vérification, réinitialisation, notifications)
 */
public interface EmailService {
    
    /**
     * Envoie un email de vérification d'adresse
     * @param utilisateur Utilisateur destinataire
     * @param verificationToken Token de vérification
     */
    void sendEmailVerification(Utilisateur utilisateur, String verificationToken);
    
    /**
     * Envoie un email de réinitialisation de mot de passe
     * @param utilisateur Utilisateur destinataire
     * @param resetToken Token de réinitialisation
     */
    void sendPasswordResetEmail(Utilisateur utilisateur, String resetToken);
    
    /**
     * Envoie un email de bienvenue après inscription
     * @param utilisateur Nouvel utilisateur
     */
    void sendWelcomeEmail(Utilisateur utilisateur);
    
    /**
     * Envoie un email de confirmation après vérification
     * @param utilisateur Utilisateur dont l'email a été vérifié
     */
    void sendEmailVerificationConfirmation(Utilisateur utilisateur);
    
    /**
     * Envoie un email de confirmation après changement de mot de passe
     * @param utilisateur Utilisateur ayant changé son mot de passe
     */
    void sendPasswordChangeConfirmation(Utilisateur utilisateur);
    
    /**
     * Envoie un email d'alerte de sécurité
     * @param utilisateur Utilisateur concerné
     * @param alertType Type d'alerte (connexion suspecte, changement de mot de passe, etc.)
     * @param details Détails de l'alerte
     */
    void sendSecurityAlert(Utilisateur utilisateur, String alertType, Map<String, Object> details);
    
    /**
     * Envoie un email de notification de suspension de compte
     * @param utilisateur Utilisateur suspendu
     * @param reason Raison de la suspension
     */
    void sendAccountSuspensionNotification(Utilisateur utilisateur, String reason);
    
    /**
     * Envoie un email de notification de réactivation de compte
     * @param utilisateur Utilisateur réactivé
     */
    void sendAccountReactivationNotification(Utilisateur utilisateur);
    
    /**
     * Envoie un email personnalisé avec template
     * @param to Adresse email destinataire
     * @param subject Sujet de l'email
     * @param templateName Nom du template à utiliser
     * @param templateVariables Variables à injecter dans le template
     */
    void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> templateVariables);
    
    /**
     * Envoie un email simple en texte brut
     * @param to Adresse email destinataire
     * @param subject Sujet de l'email
     * @param content Contenu de l'email
     */
    void sendSimpleEmail(String to, String subject, String content);
    
    /**
     * Envoie un email HTML
     * @param to Adresse email destinataire
     * @param subject Sujet de l'email
     * @param htmlContent Contenu HTML de l'email
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * Envoie un email avec pièce jointe
     * @param to Adresse email destinataire
     * @param subject Sujet de l'email
     * @param content Contenu de l'email
     * @param attachmentPath Chemin vers la pièce jointe
     * @param attachmentName Nom de la pièce jointe
     */
    void sendEmailWithAttachment(String to, String subject, String content, String attachmentPath, String attachmentName);
    
    /**
     * Valide une adresse email
     * @param email Adresse email à valider
     * @return true si l'email est valide
     */
    boolean isValidEmail(String email);
    
    /**
     * Vérifie si un email peut être envoyé (pas dans la blacklist, etc.)
     * @param email Adresse email à vérifier
     * @return true si l'email peut être envoyé
     */
    boolean canSendEmail(String email);
    
    /**
     * Génère l'URL de vérification d'email
     * @param token Token de vérification
     * @return URL complète de vérification
     */
    String generateEmailVerificationUrl(String token);
    
    /**
     * Génère l'URL de réinitialisation de mot de passe
     * @param token Token de réinitialisation
     * @return URL complète de réinitialisation
     */
    String generatePasswordResetUrl(String token);
    
    /**
     * Envoie un email de notification pour les tuteurs
     * @param utilisateur Tuteur destinataire
     * @param notificationType Type de notification
     * @param data Données de la notification
     */
    void sendTutorNotification(Utilisateur utilisateur, String notificationType, Map<String, Object> data);
    
    /**
     * Envoie un email de notification pour les étudiants
     * @param utilisateur Étudiant destinataire
     * @param notificationType Type de notification
     * @param data Données de la notification
     */
    void sendStudentNotification(Utilisateur utilisateur, String notificationType, Map<String, Object> data);
    
    /**
     * Envoie un email de notification pour les parents
     * @param utilisateur Parent destinataire
     * @param notificationType Type de notification
     * @param data Données de la notification
     */
    void sendParentNotification(Utilisateur utilisateur, String notificationType, Map<String, Object> data);
}
