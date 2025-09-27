package com.backend.tutor_app.model.support;


import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.User;
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

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "provider_email")
    private String providerEmail;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;


    //relations
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;







}
