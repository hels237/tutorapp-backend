package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.ReportStatus;
import com.backend.tutor_app.model.enums.ReportType;
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

/**
 * Entité représentant un signalement de modération
 * Utilisée par AdminController pour gérer les signalements d'utilisateurs
 */
@Entity
@Table(name = "moderation_reports")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationReport extends AbstractEntiity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @NotNull(message = "Le rapporteur est requis")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    @NotNull(message = "L'utilisateur signalé est requis")
    private User reportedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Le type de signalement est requis")
    private ReportType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "La raison du signalement est requise")
    @Size(min = 10, max = 500, message = "La raison doit contenir entre 10 et 500 caractères")
    private String reason;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @Column(name = "evidence_url")
    private String evidenceUrl;

    @Column(name = "admin_comment", columnDefinition = "TEXT")
    private String adminComment;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt = LocalDateTime.now();

    @Column(name = "priority_level")
    private Integer priorityLevel = 1; // 1=Low, 2=Medium, 3=High, 4=Critical

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "reporter_ip")
    private String reporterIp;

    @Column(name = "related_content_id")
    private Long relatedContentId; // ID du contenu signalé (cours, message, etc.)

    @Column(name = "related_content_type")
    private String relatedContentType; // Type du contenu signalé

    // Méthodes utilitaires
    public boolean isPending() {
        return status == ReportStatus.PENDING;
    }

    public boolean isUnderReview() {
        return status == ReportStatus.UNDER_REVIEW;
    }

    public boolean isResolved() {
        return status == ReportStatus.RESOLVED;
    }

    public boolean isDismissed() {
        return status == ReportStatus.DISMISSED;
    }

    public void resolve(Long adminId, String comment) {
        this.status = ReportStatus.RESOLVED;
        this.resolvedBy = adminId;
        this.resolvedAt = LocalDateTime.now();
        this.adminComment = comment;
    }

    public void dismiss(Long adminId, String comment) {
        this.status = ReportStatus.DISMISSED;
        this.resolvedBy = adminId;
        this.resolvedAt = LocalDateTime.now();
        this.adminComment = comment;
    }

    public void setUnderReview(Long adminId) {
        this.status = ReportStatus.UNDER_REVIEW;
        this.resolvedBy = adminId;
    }

    public boolean isHighPriority() {
        return priorityLevel != null && priorityLevel >= 3;
    }

    public boolean isCritical() {
        return priorityLevel != null && priorityLevel == 4;
    }

    public String getPriorityLabel() {
        if (priorityLevel == null) return "Non défini";
        return switch (priorityLevel) {
            case 1 -> "Faible";
            case 2 -> "Moyen";
            case 3 -> "Élevé";
            case 4 -> "Critique";
            default -> "Non défini";
        };
    }

    public boolean canBeResolvedBy(User admin) {
        return admin != null && admin.getRole().name().equals("ADMIN");
    }

    public long getDaysSinceReported() {
        if (reportedAt == null) return 0;
        return java.time.Duration.between(reportedAt, LocalDateTime.now()).toDays();
    }
}
