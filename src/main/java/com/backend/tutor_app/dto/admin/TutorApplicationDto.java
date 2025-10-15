package com.backend.tutor_app.dto.admin;

import com.backend.tutor_app.model.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour les demandes de tuteur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorApplicationDto {
    
    private Long id;
    private Long userId;
    
    // Informations utilisateur
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private String userProfilePicture;
    
    // Informations de la demande
    private ApplicationStatus status;
    private String motivation;
    private String experience;
    private String education;
    private List<String> subjects;
    private List<String> languages;
    private Double hourlyRate;
    private String availability;
    
    // Documents
    private List<String> documents; // URLs des documents uploadés
    private String cv;
    private String diploma;
    private String certification;
    
    // Informations de traitement
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy; // ID de l'admin qui a traité
    private String reviewerName;
    private String reviewComment;
    private String rejectionReason;
    
    // Métadonnées
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}
