package com.backend.tutor_app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la mise à jour des informations personnelles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalInfoRequest {

    @NotBlank(message = "Le prénom est requis")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    @Email(message = "Format d'email invalide")
    private String email;

    @Size(max = 15, message = "Le numéro de téléphone ne peut pas dépasser 15 caractères")
    private String phoneNumber;

    @Size(max = 500, message = "La bio ne peut pas dépasser 500 caractères")
    private String bio;

    @Size(max = 100, message = "La ville ne peut pas dépasser 100 caractères")
    private String city;

    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String country;
}
