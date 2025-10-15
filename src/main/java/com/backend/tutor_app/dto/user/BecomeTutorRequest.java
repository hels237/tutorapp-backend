package com.backend.tutor_app.dto.user;

import com.backend.tutor_app.model.AbstractEntiity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la demande de devenir tuteur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BecomeTutorRequest  {

    @NotBlank(message = "La description est requise")
    @Size(min = 50, max = 1000, message = "La description doit contenir entre 50 et 1000 caractères")
    private String description;

    @NotEmpty(message = "Au moins une matière doit être sélectionnée")
    private List<String> subjects;

    @NotEmpty(message = "Au moins un niveau doit être sélectionné")
    private List<String> levels;

    @NotNull(message = "Le prix par heure est requis")
    private Double hourlyRate;

    @NotBlank(message = "Le niveau d'éducation est requis")
    private String educationLevel;

    @NotBlank(message = "L'institution est requise")
    private String institution;

    @NotBlank(message = "Le domaine d'étude est requis")
    private String fieldOfStudy;

    @NotNull(message = "L'année de graduation est requise")
    private Integer graduationYear;

    @NotNull(message = "Les années d'expérience sont requises")
    private Integer yearsOfExperience;

    @Size(max = 500, message = "L'expérience d'enseignement ne peut pas dépasser 500 caractères")
    private String teachingExperience;

    @NotEmpty(message = "Au moins une langue doit être sélectionnée")
    private List<String> languages;

    private List<String> certifications;

    private Boolean hasTeachingCertification;

    private Boolean acceptsTerms;
}
