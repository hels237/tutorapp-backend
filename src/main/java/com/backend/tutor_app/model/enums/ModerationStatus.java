package com.backend.tutor_app.model.enums;

/**
 * Statut des signalements de modération
 */
public enum ModerationStatus {
    PENDING("En attente"),
    UNDER_REVIEW("En cours d'examen"),
    RESOLVED("Résolu"),
    DISMISSED("Rejeté"),
    ESCALATED("Escaladé"),
    CLOSED("Fermé");

    private final String displayName;

    ModerationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
