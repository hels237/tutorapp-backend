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

@Setter @Getter
@Entity
@Table(name = "refresh_tokens")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends AbstractEntiity {

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    // Constructeur utilitaire
    public RefreshToken(User user, String token, int expirationDays) {
        this.user = user;
        this.token = token;
        this.expiresAt = LocalDateTime.now().plusDays(expirationDays);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    public void revoke() {
        this.isRevoked = true;
    }

    public void updateLastUsed() {
        this.lastUsed = LocalDateTime.now();
    }
}
