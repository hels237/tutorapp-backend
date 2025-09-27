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

@Setter
@Getter
@Entity
@SuperBuilder
@Table(name = "email_verification_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken  extends AbstractEntiity {

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // Relations
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}
