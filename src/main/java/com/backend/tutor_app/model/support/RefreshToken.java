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
    private Utilisateur utilisateur;

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

    // (Q) PHASE 1 - ÉTAPE 1.2 : Métadonnées enrichies pour traçabilité et sécurité
    
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0; // (Q) Nombre de fois que le token a été utilisé
    
    @Column(name = "parent_token_id")
    private Long parentTokenId; // (Q) ID du token parent pour tracer la chaîne de rotation
    
    @Column(name = "browser_name", length = 100)
    private String browserName; // (Q) Ex: "Chrome", "Firefox", "Safari"
    
    @Column(name = "browser_version", length = 50)
    private String browserVersion; // (Q) Ex: "120.0.6099.109"
    
    @Column(name = "os_name", length = 100)
    private String osName; // (Q) Ex: "Windows", "macOS", "Linux"
    
    @Column(name = "os_version", length = 50)
    private String osVersion; // (Q) Ex: "10.0", "14.2.1"
    
    @Column(name = "timezone", length = 100)
    private String timezone; // (Q) Ex: "Europe/Paris", "America/New_York"
    
    @Column(name = "browser_language", length = 10)
    private String browserLanguage; // (Q) Ex: "fr-FR", "en-US"
    
    @Column(name = "user_agent", length = 1000)
    private String userAgent; // (Q) User Agent complet pour analyse détaillée
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt; // (Q) Date de révocation pour audit
    
    @Column(name = "revoked_reason", length = 255)
    private String revokedReason; // (Q) Raison de la révocation (ROTATED, LOGOUT, SECURITY, etc.)

    // Constructeur utilitaire
    public RefreshToken(Utilisateur utilisateur, String token, int expirationDays) {
        this.utilisateur = utilisateur;
        this.token = token;
        this.expiresAt = LocalDateTime.now().plusDays(expirationDays);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked && !isExpired();
    }

    // (Q) PHASE 1 - ÉTAPE 1.2 : Méthodes utilitaires améliorées
    
    public void revoke() {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now(); // (Q) Enregistrer la date de révocation
    }
    
    // (Q) Révocation avec raison pour audit et traçabilité
    public void revokeWithReason(String reason) {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }

    public void updateLastUsed() {
        this.lastUsed = LocalDateTime.now();
        this.usageCount++; // (Q) Incrémenter le compteur d'utilisation
    }
    
    // (Q) Méthode pour incrémenter le compteur d'utilisation
    public void incrementUsageCount() {
        this.usageCount++;
        this.lastUsed = LocalDateTime.now();
    }
}
