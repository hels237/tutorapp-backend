package com.backend.tutor_app.dto.Auth;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour exposer un utilisateur côté API/Admin.
 * Fourni avec des helpers de mapping depuis/vers l'entité JPA `User`.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePicture;

    // Stockés en String pour simplicité d'échange API
    private String role;           // ex: "ADMIN", "TUTOR", "STUDENT", "PARENT"
    private String status;         // ex: "ACTIVE", "SUSPENDED", "INACTIVE", "PENDING_VERIFICATION"
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    // ======================== Mappers ========================

    /**
     * Construit un UserDto à partir d'une entité User.
     */
    public static UserDto fromEntity(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        return UserDto.builder()
                .id(utilisateur.getId())
                .email(utilisateur.getEmail())
                .firstName(utilisateur.getFirstName())
                .lastName(utilisateur.getLastName())
                .phoneNumber(utilisateur.getPhoneNumber())
                .profilePicture(utilisateur.getProfilePicture())
                .role(utilisateur.getRole() != null ? utilisateur.getRole().name() : null)
                .status(utilisateur.getStatus() != null ? utilisateur.getStatus().name() : null)
                .emailVerified(utilisateur.getEmailVerified())
                .createdAt(utilisateur.getCreatedAt())
                .updatedAt(utilisateur.getLastUpdate())
                .lastLogin(utilisateur.getLastLogin())
                .build();
    }

    /**
     * Construit une entité User à partir d'un UserDto.
     * REMARQUES IMPORTANTES:
     * - Ne définit PAS le mot de passe (doit être géré par un flux dédié)
     * - Ne définit PAS les relations (tokens, etc.)
     * - Les champs enums `role` et `status` sont parsés depuis leurs valeurs String
     */
    public static Utilisateur toEntity(UserDto userDto) {
        if (userDto == null) return null;
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userDto.getId());
        utilisateur.setEmail(userDto.getEmail());
        utilisateur.setFirstName(userDto.getFirstName());
        utilisateur.setLastName(userDto.getLastName());
        utilisateur.setPhoneNumber(userDto.getPhoneNumber());
        utilisateur.setProfilePicture(userDto.getProfilePicture());
        if (userDto.getRole() != null) {
            try { utilisateur.setRole(Role.valueOf(userDto.getRole().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        if (userDto.getStatus() != null) {
            try { utilisateur.setStatus(UserStatus.valueOf(userDto.getStatus().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        utilisateur.setEmailVerified(userDto.getEmailVerified() != null ? userDto.getEmailVerified() : Boolean.FALSE);
        utilisateur.setCreatedAt(userDto.getCreatedAt());
        utilisateur.setLastUpdate(userDto.getUpdatedAt());
        utilisateur.setLastLogin(userDto.getLastLogin());
        return utilisateur;
    }
}
