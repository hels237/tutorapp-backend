package com.backend.tutor_app.model;


import com.backend.tutor_app.model.enums.AdminLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends Utilisateur {

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level")
    private AdminLevel adminLevel = AdminLevel.ADMIN;

//#################################################################


    @Column(name = "department")
    private String department;

    @Column(name = "permissions", columnDefinition = "TEXT")
    private String permissions; // JSON des permissions spécifiques

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "can_manage_users", nullable = false)
    private Boolean canManageUsers = true;

    @Column(name = "can_manage_tutors", nullable = false)
    private Boolean canManageTutors = true;

    @Column(name = "can_view_reports", nullable = false)
    private Boolean canViewReports = true;

    @Column(name = "can_manage_content", nullable = false)
    private Boolean canManageContent = false;

    // Méthodes utilitaires
    public boolean hasPermission(String permission) {
        // Logique de vérification des permissions
        return adminLevel == AdminLevel.SUPER_ADMIN ||
                (permissions != null && permissions.contains(permission));
    }

}
