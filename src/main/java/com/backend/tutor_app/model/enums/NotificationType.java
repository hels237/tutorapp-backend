package com.backend.tutor_app.model.enums;

/**
 * Types de notifications dans TutorApp
 */
public enum NotificationType {
    // Sécurité
    SECURITY_ALERT("Alerte de sécurité"),
    SECURITY_LOGIN("Nouvelle connexion"),
    SECURITY_PASSWORD_CHANGED("Mot de passe modifié"),
    SECURITY_ACCOUNT_LOCKED("Compte verrouillé"),
    
    // Réservations
    NEW_BOOKING("Nouvelle réservation"),
    BOOKING_CONFIRMED("Réservation confirmée"),
    BOOKING_CANCELLED("Réservation annulée"),
    BOOKING_REMINDER("Rappel de cours"),
    BOOKING_COMPLETED("Cours terminé"),
    
    // Messages
    NEW_MESSAGE("Nouveau message"),
    MESSAGE_REPLY("Réponse à un message"),
    
    // Paiements
    PAYMENT_SUCCESS("Paiement réussi"),
    PAYMENT_FAILED("Paiement échoué"),
    PAYMENT_REFUND("Remboursement effectué"),
    
    // Système
    SYSTEM_UPDATE("Mise à jour système"),
    SYSTEM_MAINTENANCE("Maintenance planifiée"),
    SYSTEM_ANNOUNCEMENT("Annonce"),
    
    // Tuteur
    TUTOR_APPLICATION_APPROVED("Candidature approuvée"),
    TUTOR_APPLICATION_REJECTED("Candidature rejetée"),
    TUTOR_DOCUMENT_VERIFIED("Document vérifié"),
    TUTOR_DOCUMENT_REJECTED("Document rejeté"),
    TUTOR_NEW_REVIEW("Nouvel avis reçu"),
    
    // Étudiant
    STUDENT_LESSON_REMINDER("Rappel de cours"),
    STUDENT_HOMEWORK_ASSIGNED("Devoir assigné"),
    STUDENT_GRADE_POSTED("Note publiée"),
    
    // Parent
    PARENT_CHILD_ACTIVITY("Activité de l'enfant"),
    PARENT_PAYMENT_DUE("Paiement à effectuer"),
    
    // Admin
    ADMIN_NEW_USER("Nouvel utilisateur"),
    ADMIN_REPORT_SUBMITTED("Signalement reçu"),
    ADMIN_ACTION_REQUIRED("Action requise");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
