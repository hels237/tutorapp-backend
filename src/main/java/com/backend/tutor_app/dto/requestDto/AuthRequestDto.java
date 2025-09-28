package com.backend.tutor_app.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequestDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank @Size(min = 8)
    private String password;

    private Boolean rememberMe;
}
