package com.backend.tutor_app.model.enums;

import lombok.Getter;

@Getter
public enum Role {
    STUDENT("Étudiant", "ROLE_STUDENT"),
    TUTOR("Tuteur", "ROLE_TUTOR"),
    PARENT("Parent", "ROLE_PARENT"),
    ADMIN("Administrateur", "ROLE_ADMIN");

    private final String displayName;
    private final String authority;

    Role(String displayName, String authority) {
        this.displayName = displayName;
        this.authority = authority;
    }
}
