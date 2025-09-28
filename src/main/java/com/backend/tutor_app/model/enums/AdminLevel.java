package com.backend.tutor_app.model.enums;

import lombok.Getter;

@Getter
public enum AdminLevel {
    SUPER_ADMIN("Super Administrateur", 100),
    ADMIN("Administrateur", 50),
    MODERATOR("Modérateur", 25);

    private final String displayName;
    private final int level;

    AdminLevel(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }


    public boolean hasHigherLevelThan(AdminLevel other) {
        return this.level > other.level;
    }
}
