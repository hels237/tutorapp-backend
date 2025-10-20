package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une demande pour devenir tuteur
 * Utilisée par AdminController pour gérer les candidatures de tuteurs
 */
@Entity
@Table(name = "tutor_applications")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TutorApplication extends AbstractEntiity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @NotNull(message = "L'utilisateur est requis")
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "La motivation est requise")
    @Size(min = 50, max = 1000, message = "La motivation doit contenir entre 50 et 1000 caractères")
    private String motivation;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "L'expérience ne peut pas dépasser 2000 caractères")
    private String experience;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @ElementCollection
    @CollectionTable(
        name = "application_subjects",
        joinColumns = @JoinColumn(name = "application_id")
    )
    @Column(name = "subject")
    private List<String> subjects = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "application_levels",
        joinColumns = @JoinColumn(name = "application_id")
    )
    @Column(name = "level")
    private List<String> levels = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "application_qualifications",
        joinColumns = @JoinColumn(name = "application_id")
    )
    @Column(name = "qualification")
    private List<String> qualifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "application_languages",
        joinColumns = @JoinColumn(name = "application_id")
    )
    @Column(name = "language")
    private List<String> languages = new ArrayList<>();

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "available_hours_per_week")
    private Integer availableHoursPerWeek;

    @Column(name = "has_teaching_experience")
    private Boolean hasTeachingExperience = false;

    @Column(name = "has_certifications")
    private Boolean hasCertifications = false;

    @Column(name = "admin_comment", columnDefinition = "TEXT")
    private String adminComment;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    // Méthodes utilitaires
    public boolean isPending() {
        return status == ApplicationStatus.PENDING;
    }

    public boolean isApproved() {
        return status == ApplicationStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == ApplicationStatus.REJECTED;
    }

    public boolean isUnderReview() {
        return status == ApplicationStatus.UNDER_REVIEW;
    }

    public void approve(Long adminId, String comment) {
        this.status = ApplicationStatus.APPROVED;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.adminComment = comment;
    }

    public void reject(Long adminId, String comment) {
        this.status = ApplicationStatus.REJECTED;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.adminComment = comment;
    }

    public void setUnderReview(Long adminId) {
        this.status = ApplicationStatus.UNDER_REVIEW;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
    }

    public String getSubjectsAsString() {
        return subjects != null ? String.join(", ", subjects) : "";
    }

    public String getLevelsAsString() {
        return levels != null ? String.join(", ", levels) : "";
    }

    public String getQualificationsAsString() {
        return qualifications != null ? String.join(", ", qualifications) : "";
    }

    public String getLanguagesAsString() {
        return languages != null ? String.join(", ", languages) : "";
    }
}
