package com.backend.tutor_app.model.enums;

/**
 * Statut des signalements de modération
 */
public enum ReportStatus {
    PENDING("En attente"),
    UNDER_REVIEW("En cours d'examen"),
    RESOLVED("Résolu"),
    DISMISSED("Rejeté"),
    ESCALATED("Escaladé");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
