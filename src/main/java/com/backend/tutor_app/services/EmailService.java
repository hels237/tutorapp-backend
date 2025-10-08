package com.backend.tutor_app.services;

import com.backend.tutor_app.model.User;

import java.util.Map;

/**
 * Service d'envoi d'emails pour TutorApp
 * Gère tous les types d'emails (vérification, réinitialisation, notifications)
 */
public interface EmailService {
    
    /**
     * Envoie un email de vérification d'adresse
     * @param user Utilisateur destinataire
     * @param verificationToken Token de vérification
     */
    void sendEmailVerification(User user, String verificationToken);
    
    /**
     * Envoie un email de réinitialisation de mot de passe
     * @param user Utilisateur destinataire
     * @param resetToken Token de réinitialisation
     */
    void sendPasswordResetEmail(User user, String resetToken);
    
    /**
     * Envoie un email de bienvenue après inscription
     * @param user Nouvel utilisateur
     */
    void sendWelcomeEmail(User user);
    
    /**
     * Envoie un email de confirmation après vérification
     * @param user Utilisateur dont l'email a été vérifié
     */
    void sendEmailVerificationConfirmation(User user);
    
    /**
     * Envoie un email de confirmation après changement de mot de passe
     * @param user Utilisateur ayant changé son mot de passe
     */
    void sendPasswordChangeConfirmation(User user);
    
    /**
     * Envoie un email d'alerte de sécurité
     * @param user Utilisateur concerné
     * @param alertType Type d'alerte (connexion suspecte, changement de mot de passe, etc.)
     * @param details Détails de l'alerte
     */
    void sendSecurityAlert(User user, String alertType, Map<String, Object> details);
    
    /**
     * Envoie un email de notification de suspension de compte
     * @param user Utilisateur suspendu
     * @param reason Raison de la suspension
     */
    void sendAccountSuspensionNotification(User user, String reason);
    
    /**
     * Envoie un email de notification de réactivation de compte
     * @param user Utilisateur réactivé
     */
    void sendAccountReactivationNotification(User user);
    
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
     * @param user Tuteur destinataire
     * @param notificationType Type de notification
     * @param data Données de la notification
     */
    void sendTutorNotification(User user, String notificationType, Map<String, Object> data);
    
    /**
     * Envoie un email de notification pour les étudiants
     * @param user Étudiant destinataire
     * @param notificationType Type de notification
     * @param data Données de la notification
     */
    void sendStudentNotification(User user, String notificationType, Map<String, Object> data);
    
    /**
     * Envoie un email de notification pour les parents
     * @param user Parent destinataire
     * @param notificationType Type de notification
     * @param data Données de la notification
     */
    void sendParentNotification(User user, String notificationType, Map<String, Object> data);
}
