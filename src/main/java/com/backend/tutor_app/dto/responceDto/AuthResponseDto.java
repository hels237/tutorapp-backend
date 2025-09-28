package com.backend.tutor_app.dto.responceDto;

import com.backend.tutor_app.dto.UserProfileDto;

public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserProfileDto user;
}
