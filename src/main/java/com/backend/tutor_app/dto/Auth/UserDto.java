package com.backend.tutor_app.dto.Auth;

import com.backend.tutor_app.model.User;
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
    public static UserDto fromEntity(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getLastUpdate())
                .lastLogin(user.getLastLogin())
                .build();
    }

    /**
     * Construit une entité User à partir d'un UserDto.
     * REMARQUES IMPORTANTES:
     * - Ne définit PAS le mot de passe (doit être géré par un flux dédié)
     * - Ne définit PAS les relations (tokens, etc.)
     * - Les champs enums `role` et `status` sont parsés depuis leurs valeurs String
     */
    public static User toEntity(UserDto userDto) {
        if (userDto == null) return null;
        User user = new User();
        user.setId(userDto.getId());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setProfilePicture(userDto.getProfilePicture());
        if (userDto.getRole() != null) {
            try { user.setRole(Role.valueOf(userDto.getRole().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        if (userDto.getStatus() != null) {
            try { user.setStatus(UserStatus.valueOf(userDto.getStatus().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        user.setEmailVerified(userDto.getEmailVerified() != null ? userDto.getEmailVerified() : Boolean.FALSE);
        user.setCreatedAt(userDto.getCreatedAt());
        user.setLastUpdate(userDto.getUpdatedAt());
        user.setLastLogin(userDto.getLastLogin());
        return user;
    }
}
