package com.backend.tutor_app.model.support;

import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@SuperBuilder
@Table(name = "password_reset_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken extends AbstractEntiity {

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;


    // Relation with User entity
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}
