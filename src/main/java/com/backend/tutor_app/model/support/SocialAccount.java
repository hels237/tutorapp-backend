package com.backend.tutor_app.model.support;


import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "social_accounts")
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccount extends AbstractEntiity {

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "provider_name", length = 100)
    private String providerName;

    @Column(name = "provider_avatar")
    private String providerAvatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "provider_id", nullable = false,length = 100)
    private String providerId;

    @Column(name = "provider_email", length = 100)
    private String providerEmail;

    //relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

//##################################################################

    // Méthodes utilitaires
    public boolean isTokenExpired() {
        return tokenExpiresAt != null && LocalDateTime.now().isAfter(tokenExpiresAt);
    }





}
