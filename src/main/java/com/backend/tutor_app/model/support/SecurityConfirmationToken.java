package com.backend.tutor_app.model.support;

import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Utilisateur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * (PHASE 3 - Priorité 3) Token de confirmation de sécurité
 * Utilisé pour confirmer l'identité de l'utilisateur après une activité suspecte
 */
@Entity
@Table(name = "security_confirmation_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SecurityConfirmationToken extends AbstractEntiity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "confirmation_ip", length = 45)
    private String confirmationIp;

    @Column(name = "confirmation_user_agent", length = 500)
    private String confirmationUserAgent;

    /**
     * Vérifie si le token est expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Vérifie si le token est valide (non utilisé et non expiré)
     */
    public boolean isValid() {
        return !isUsed && !isExpired();
    }

    /**
     * Marque le token comme confirmé
     */
    public void markAsConfirmed(String confirmationIp, String confirmationUserAgent) {
        this.confirmedAt = LocalDateTime.now();
        this.isUsed = true;
        this.confirmationIp = confirmationIp;
        this.confirmationUserAgent = confirmationUserAgent;
    }
}
