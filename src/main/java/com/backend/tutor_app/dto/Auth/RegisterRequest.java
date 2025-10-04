package com.backend.tutor_app.dto.Auth;


import com.backend.tutor_app.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Le prénom est requis")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
            message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un chiffre et un caractère spécial")
    private String password;

    @NotBlank(message = "La confirmation du mot de passe est requise")
    private String confirmPassword;

    @NotNull(message = "Le type d'utilisateur est requis")
    private Role userType;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Format de numéro de téléphone invalide")
    private String phoneNumber;

    // Champs spécifiques Student
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate birthDate;

    @Size(max = 50, message = "Le niveau scolaire ne peut pas dépasser 50 caractères")
    private String schoolLevel;

    @Size(max = 100, message = "Le nom de l'école ne peut pas dépasser 100 caractères")
    private String schoolName;

    @Size(max = 200, message = "L'adresse ne peut pas dépasser 200 caractères")
    private String address;

    // Champs spécifiques Tutor
    @Size(max = 2000, message = "La biographie ne peut pas dépasser 2000 caractères")
    private String bio;

    @DecimalMin(value = "0.0", message = "Le tarif horaire doit être positif")
    @DecimalMax(value = "1000.0", message = "Le tarif horaire ne peut pas dépasser 1000€")
    private BigDecimal hourlyRate;

    @Min(value = 0, message = "L'expérience ne peut pas être négative")
    @Max(value = 50, message = "L'expérience ne peut pas dépasser 50 ans")
    private Integer experienceYears;

    @Size(max = 100, message = "La localisation ne peut pas dépasser 100 caractères")
    private String location;

    // Champs spécifiques Parent
    @Size(max = 100, message = "La profession ne peut pas dépasser 100 caractères")
    private String occupation;

    @Size(max = 100, message = "Le contact d'urgence ne peut pas dépasser 100 caractères")
    private String emergencyContact;

    // Validation personnalisée
    @AssertTrue(message = "Les mots de passe ne correspondent pas")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    // Méthodes utilitaires
    public boolean isStudent() {
        return Role.STUDENT.equals(userType);
    }

    public boolean isTutor() {
        return Role.TUTOR.equals(userType);
    }

    public boolean isParent() {
        return Role.PARENT.equals(userType);
    }
}
