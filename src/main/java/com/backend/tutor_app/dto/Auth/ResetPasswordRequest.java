package com.backend.tutor_app.dto.Auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8)
    private String newPassword;

    @NotBlank
    private String confirmPassword;

    @AssertTrue(message = "Les mots de passe ne correspondent pas")
    public boolean isPasswordMatching() { return newPassword != null && newPassword.equals(confirmPassword); }
}
