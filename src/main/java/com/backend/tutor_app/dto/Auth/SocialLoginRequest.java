package com.backend.tutor_app.dto.Auth;


import com.backend.tutor_app.model.enums.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SocialLoginRequest {

    @NotNull
    private SocialProvider provider; // GOOGLE, FACEBOOK, GITHUB

    // code d’autorisation
    @NotBlank
    private String code;

    private String redirectUri;
}
