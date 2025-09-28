package com.backend.tutor_app.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequestDto {
    @Email
    @NotBlank
    private String email;
}
