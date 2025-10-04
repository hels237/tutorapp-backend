package com.backend.tutor_app.dto.Auth;

import com.backend.tutor_app.dto.user.UserProfileDto;
import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserProfileDto user;

    //############################W

}
