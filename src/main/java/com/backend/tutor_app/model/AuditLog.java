package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.AuditAction;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Entité représentant un log d'audit pour tracer les actions importantes
 * Utilisée par AdminController pour consulter l'historique des actions
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_admin_id", columnList = "admin_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends AbstractEntiity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull(message = "L'action est requise")
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 100)
    @NotBlank(message = "Le type d'entité est requis")
    @Size(max = 100, message = "Le type d'entité ne peut pas dépasser 100 caractères")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "Les détails ne peuvent pas dépasser 5000 caractères")
    private String details;

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON des anciennes valeurs

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON des nouvelles valeurs

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "request_id", length = 100)
    private String requestId; // Pour tracer les requêtes

    @Column(name = "severity_level")
    private Integer severityLevel = 1; // 1=Info, 2=Warning, 3=Error, 4=Critical

    @Column(name = "success")
    private Boolean success = true;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    // Méthodes utilitaires
    public boolean isUserAction() {
        return userId != null;
    }

    public boolean isAdminAction() {
        return adminId != null;
    }

    public boolean isSystemAction() {
        return userId == null && adminId == null;
    }

    public boolean isSuccessful() {
        return success != null && success;
    }

    public boolean isFailed() {
        return success != null && !success;
    }

    public boolean isHighSeverity() {
        return severityLevel != null && severityLevel >= 3;
    }

    public boolean isCritical() {
        return severityLevel != null && severityLevel == 4;
    }

    public String getSeverityLabel() {
        if (severityLevel == null) return "Non défini";
        return switch (severityLevel) {
            case 1 -> "Info";
            case 2 -> "Avertissement";
            case 3 -> "Erreur";
            case 4 -> "Critique";
            default -> "Non défini";
        };
    }

    public String getActorType() {
        if (adminId != null) return "Admin";
        if (userId != null) return "User";
        return "System";
    }

    public Long getActorId() {
        if (adminId != null) return adminId;
        if (userId != null) return userId;
        return null;
    }

    public void markAsSuccess() {
        this.success = true;
        this.errorMessage = null;
    }

    public void markAsFailure(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        if (this.severityLevel == null || this.severityLevel < 2) {
            this.severityLevel = 2; // Au minimum Warning pour les échecs
        }
    }

    public void setSeverityInfo() {
        this.severityLevel = 1;
    }

    public void setSeverityWarning() {
        this.severityLevel = 2;
    }

    public void setSeverityError() {
        this.severityLevel = 3;
    }

    public void setSeverityCritical() {
        this.severityLevel = 4;
    }

    public long getDaysAgo() {
        if (timestamp == null) return 0;
        return java.time.Duration.between(timestamp, LocalDateTime.now()).toDays();
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "";
        return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    // Méthodes statiques pour créer des logs d'audit
    public static AuditLog createUserAction(Long userId, AuditAction action, String entityType, Long entityId, String details) {
        return AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .severityLevel(1)
                .success(true)
                .build();
    }

    public static AuditLog createAdminAction(Long adminId, AuditAction action, String entityType, Long entityId, String details) {
        return AuditLog.builder()
                .adminId(adminId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .severityLevel(2) // Admin actions are at least Warning level
                .success(true)
                .build();
    }

    public static AuditLog createSystemAction(AuditAction action, String entityType, String details) {
        return AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .details(details)
                .timestamp(LocalDateTime.now())
                .severityLevel(1)
                .success(true)
                .build();
    }
}
