package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.Auth.AuthRequest;
import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.dto.Auth.RegisterRequest;
import com.backend.tutor_app.dto.Auth.ResetPasswordRequest;
import com.backend.tutor_app.model.enums.SocialProvider;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    AuthResponse register(RegisterRequest request);
    void logout(String token);
    AuthResponse refreshToken(String refreshToken);
    void sendEmailVerification(String email);
    void verifyEmail(String token);
    void sendPasswordReset(String email);
    void resetPassword(ResetPasswordRequest request);
    AuthResponse socialLogin(SocialProvider provider, String code);

}
