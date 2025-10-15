package com.backend.tutor_app.model.enums;

/**
 * Types de signalements
 */
public enum ReportType {
    INAPPROPRIATE_CONTENT("Contenu inapproprié"),
    HARASSMENT("Harcèlement"),
    SPAM("Spam"),
    FAKE_PROFILE("Faux profil"),
    INAPPROPRIATE_BEHAVIOR("Comportement inapproprié"),
    SCAM("Arnaque"),
    COPYRIGHT_VIOLATION("Violation de droits d'auteur"),
    HATE_SPEECH("Discours de haine"),
    VIOLENCE("Violence"),
    OTHER("Autre");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
