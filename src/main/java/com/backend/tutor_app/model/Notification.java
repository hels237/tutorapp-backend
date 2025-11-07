package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.NotificationPriority;
import com.backend.tutor_app.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entité Notification - Stockage des notifications utilisateur
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_user_read", columnList = "user_id, is_read"),
    @Index(name = "idx_notification_created_at", columnList = "created_at"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_priority", columnList = "priority")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    /**
     * Métadonnées JSON
     * Utilise le type JSON natif de PostgreSQL
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "action_url", length = 500)
    private String actionUrl;
    
    @Column(name = "action_label", length = 100)
    private String actionLabel;
    
    @Column(name = "icon_url", length = 500)
    private String iconUrl;
    
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "sent_via_websocket")
    @Builder.Default
    private boolean sentViaWebSocket = false;
    
    @Column(name = "sent_via_fcm")
    @Builder.Default
    private boolean sentViaFCM = false;
    
    @Column(name = "sent_via_email")
    @Builder.Default
    private boolean sentViaEmail = false;
    
    /**
     * Marque la notification comme lue
     */
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Vérifie si la notification est récente (< 24h)
     */
    public boolean isRecent() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(1));
    }
    
    /**
     * Vérifie si la notification est ancienne (> 30 jours)
     */
    public boolean isOld() {
        return createdAt.isBefore(LocalDateTime.now().minusDays(30));
    }
}
