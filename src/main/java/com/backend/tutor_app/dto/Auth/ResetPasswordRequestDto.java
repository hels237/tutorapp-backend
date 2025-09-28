package com.backend.tutor_app.dto.Auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequestDto {
    @NotBlank
    private String token;

    @NotBlank @Size(min = 8)
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}
