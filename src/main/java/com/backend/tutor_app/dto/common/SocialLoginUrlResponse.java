package com.backend.tutor_app.dto.common;

import lombok.*;

@Getter  @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginUrlResponse {
    private String authUrl;
    private String state;
    private String provider;
    private long expiresIn; // en secondes

    public static SocialLoginUrlResponse of(String authUrl, String state, String provider) {
        return SocialLoginUrlResponse.builder()
                .authUrl(authUrl)
                .state(state)
                .provider(provider)
                .expiresIn(500) // 5 minutes par défaut
                .build();
    }
}
