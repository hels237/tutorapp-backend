package com.backend.tutor_app.model.enums;

import lombok.Getter;

@Getter
public enum VerificationStatus {
    PENDING("En attente"),
    VERIFIED("Vérifié"),
    REJECTED("Rejeté"),
    EXPIRED("Expiré"),
    UNDER_REVIEW("En cours d'examen");

    private final String displayName;

    VerificationStatus(String displayName) {
        this.displayName = displayName;
    }


}
