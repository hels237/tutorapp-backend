package com.backend.tutor_app.model.support;


import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Utilisateur;
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

    @Column(name = "expires_at",nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur utilisateur;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }
    //#########################################################################


//    @Column(name = "user_agent", length = 500)
//    private String userAgent;

    // Constructeur utilitaire
    public EmailVerificationToken(Utilisateur utilisateur, String token, int expirationHours) {
        this.utilisateur = utilisateur;
        this.token = token;
        this.expiresAt = LocalDateTime.now().plusHours(expirationHours);
    }




}
