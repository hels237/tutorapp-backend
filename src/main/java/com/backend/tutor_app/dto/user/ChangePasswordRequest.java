package com.backend.tutor_app.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank private String currentPassword;
    @NotBlank @Size(min = 8) private String newPassword;
    @NotBlank
    private String confirmPassword;

    @AssertTrue(message = "Les nouveaux mots de passe ne correspondent pas")
    public boolean isPasswordMatching() { return newPassword != null && newPassword.equals(confirmPassword); }
}
