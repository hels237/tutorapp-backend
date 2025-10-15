package com.backend.tutor_app.model.enums;

/**
 * Statut des demandes de tuteur
 */
public enum ApplicationStatus {
    PENDING("En attente"),
    UNDER_REVIEW("En cours d'examen"),
    APPROVED("Approuvée"),
    REJECTED("Rejetée"),
    CANCELLED("Annulée"),
    EXPIRED("Expirée");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
