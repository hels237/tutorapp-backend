package com.backend.tutor_app.dto.notification;

import com.backend.tutor_app.model.enums.NotificationPriority;
import com.backend.tutor_app.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO pour la création d'une notification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotNull(message = "Le type de notification est obligatoire")
    private NotificationType type;
    
    @NotNull(message = "La priorité est obligatoire")
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    
    @NotBlank(message = "Le titre est obligatoire")
    private String title;
    
    @NotBlank(message = "Le message est obligatoire")
    private String message;
    
    /**
     * Métadonnées additionnelles (optionnel)
     */
    private Map<String, Object> metadata;
    
    /**
     * URL de l'action (optionnel)
     */
    private String actionUrl;
    
    /**
     * Label du bouton d'action (optionnel)
     */
    private String actionLabel;
    
    /**
     * URL de l'icône (optionnel)
     */
    private String iconUrl;
    
    /**
     * Envoyer aussi par email (optionnel, défaut: false)
     */
    @Builder.Default
    private boolean sendEmail = false;
    
    /**
     * Envoyer aussi par FCM push (optionnel, défaut: true)
     */
    @Builder.Default
    private boolean sendPush = true;
    
    /**
     * Envoyer aussi par WebSocket (optionnel, défaut: true)
     */
    @Builder.Default
    private boolean sendWebSocket = true;
}
