package com.backend.tutor_app.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    private Boolean rememberMe = false;

    private String deviceInfo; // (Q) Contiendra le User Agent complet

    private String ipAddress;
    
    // (Q) PHASE 1 - ÉTAPE 1.2 : Métadonnées enrichies envoyées par le frontend
    private String timezone;          // Ex: "Europe/Paris"
    private String browserLanguage;   // Ex: "fr-FR"
}
