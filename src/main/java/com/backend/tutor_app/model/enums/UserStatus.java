package com.backend.tutor_app.model.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("Actif"),
    INACTIVE("Inactif"),
    SUSPENDED("Suspendu"),
    PENDING_VERIFICATION("En attente de vérification"),
    LOCKED("Verrouillé"),
    DELETED("Supprimé");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
