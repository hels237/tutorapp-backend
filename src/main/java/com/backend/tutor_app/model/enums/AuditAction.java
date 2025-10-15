package com.backend.tutor_app.model.enums;

/**
 * Actions d'audit pour tracer les opérations importantes
 */
public enum AuditAction {
    // Actions utilisateur
    USER_CREATED("Utilisateur créé"),
    USER_UPDATED("Utilisateur mis à jour"),
    USER_DELETED("Utilisateur supprimé"),
    USER_STATUS_CHANGED("Statut utilisateur modifié"),
    USER_ROLE_CHANGED("Rôle utilisateur modifié"),
    USER_PASSWORD_CHANGED("Mot de passe modifié"),
    USER_EMAIL_VERIFIED("Email vérifié"),
    USER_PROFILE_PICTURE_UPDATED("Photo de profil mise à jour"),
    USER_DEACTIVATED("Compte désactivé"),
    USER_REACTIVATED("Compte réactivé"),

    // Actions d'authentification
    LOGIN_SUCCESS("Connexion réussie"),
    LOGIN_FAILED("Échec de connexion"),
    LOGOUT("Déconnexion"),
    PASSWORD_RESET_REQUESTED("Demande de réinitialisation de mot de passe"),
    PASSWORD_RESET_COMPLETED("Réinitialisation de mot de passe effectuée"),
    SOCIAL_LOGIN_SUCCESS("Connexion sociale réussie"),
    SOCIAL_ACCOUNT_LINKED("Compte social lié"),
    SOCIAL_ACCOUNT_UNLINKED("Compte social délié"),

    // Actions tuteur
    TUTOR_APPLICATION_SUBMITTED("Candidature tuteur soumise"),
    TUTOR_APPLICATION_APPROVED("Candidature tuteur approuvée"),
    TUTOR_APPLICATION_REJECTED("Candidature tuteur rejetée"),
    TUTOR_APPLICATION_UNDER_REVIEW("Candidature tuteur en examen"),
    TUTOR_PROFILE_UPDATED("Profil tuteur mis à jour"),
    TUTOR_AVAILABILITY_UPDATED("Disponibilités tuteur mises à jour"),

    // Actions de modération
    REPORT_CREATED("Signalement créé"),
    REPORT_RESOLVED("Signalement résolu"),
    REPORT_DISMISSED("Signalement rejeté"),
    REPORT_ESCALATED("Signalement escaladé"),
    USER_SUSPENDED("Utilisateur suspendu"),
    USER_BANNED("Utilisateur banni"),
    CONTENT_REMOVED("Contenu supprimé"),

    // Actions administrateur
    ADMIN_LOGIN("Connexion administrateur"),
    ADMIN_ACTION("Action administrateur"),
    ADMIN_USER_MANAGEMENT("Gestion utilisateur par admin"),
    ADMIN_SYSTEM_CONFIG("Configuration système modifiée"),
    ADMIN_BULK_ACTION("Action en lot par admin"),

    // Actions système
    SYSTEM_STARTUP("Démarrage du système"),
    SYSTEM_SHUTDOWN("Arrêt du système"),
    SYSTEM_ERROR("Erreur système"),
    SYSTEM_MAINTENANCE("Maintenance système"),
    DATA_MIGRATION("Migration de données"),
    BACKUP_CREATED("Sauvegarde créée"),
    BACKUP_RESTORED("Sauvegarde restaurée"),

    // Actions de sécurité
    SECURITY_VIOLATION("Violation de sécurité"),
    SUSPICIOUS_ACTIVITY("Activité suspecte"),
    RATE_LIMIT_EXCEEDED("Limite de taux dépassée"),
    TOKEN_REVOKED("Token révoqué"),
    SESSION_EXPIRED("Session expirée"),

    // Actions de fichiers
    FILE_UPLOADED("Fichier téléchargé"),
    FILE_DELETED("Fichier supprimé"),
    FILE_ACCESS_DENIED("Accès fichier refusé"),

    // Actions génériques
    CREATED("Créé"),
    UPDATED("Mis à jour"),
    DELETED("Supprimé"),
    VIEWED("Consulté"),
    EXPORTED("Exporté"),
    IMPORTED("Importé");

    private final String displayName;

    AuditAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isUserAction() {
        return this.name().startsWith("USER_") || 
               this.name().startsWith("LOGIN_") || 
               this.name().startsWith("LOGOUT") ||
               this.name().startsWith("PASSWORD_");
    }

    public boolean isAdminAction() {
        return this.name().startsWith("ADMIN_") ||
               this.name().contains("APPROVED") ||
               this.name().contains("REJECTED") ||
               this.name().contains("SUSPENDED") ||
               this.name().contains("BANNED");
    }

    public boolean isSecurityAction() {
        return this.name().startsWith("SECURITY_") ||
               this.name().startsWith("SUSPICIOUS_") ||
               this.name().contains("VIOLATION") ||
               this.name().contains("RATE_LIMIT");
    }

    public boolean isSystemAction() {
        return this.name().startsWith("SYSTEM_") ||
               this.name().contains("BACKUP") ||
               this.name().contains("MIGRATION");
    }

    public int getDefaultSeverityLevel() {
        if (isSecurityAction() || this.name().contains("ERROR") || this.name().contains("FAILED")) {
            return 3; // Error
        }
        if (isAdminAction() || this.name().contains("DELETED") || this.name().contains("BANNED")) {
            return 2; // Warning
        }
        return 1; // Info
    }
}
