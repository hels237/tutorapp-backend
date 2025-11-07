package com.backend.tutor_app.dto.notification;

import com.backend.tutor_app.model.enums.NotificationPriority;
import com.backend.tutor_app.model.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO pour les notifications
 * Utilisé pour l'envoi via WebSocket, FCM et la persistance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    private Long id;
    
    private Long userId;
    
    private NotificationType type;
    
    private NotificationPriority priority;
    
    private String title;
    
    private String message;
    
    /**
     * Métadonnées additionnelles (JSON)
     * Exemples :
     * - Pour sécurité : {"ip": "192.168.1.1", "device": "Chrome", "country": "France"}
     * - Pour réservation : {"reservationId": 123, "tutorName": "Jean Martin"}
     * - Pour message : {"senderId": 456, "senderName": "Marie Leroy"}
     */
    private Map<String, Object> metadata;
    
    /**
     * URL de l'action (optionnel)
     * Exemple : "/dashboard/reservations/123"
     */
    private String actionUrl;
    
    /**
     * Label du bouton d'action (optionnel)
     * Exemple : "Voir la réservation"
     */
    private String actionLabel;
    
    /**
     * URL de l'icône (optionnel)
     */
    private String iconUrl;
    
    private boolean read;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readAt;
    
    /**
     * Indique si la notification a été envoyée via WebSocket
     */
    private boolean sentViaWebSocket;
    
    /**
     * Indique si la notification a été envoyée via FCM
     */
    private boolean sentViaFCM;
    
    /**
     * Indique si la notification a été envoyée par email
     */
    private boolean sentViaEmail;
}
