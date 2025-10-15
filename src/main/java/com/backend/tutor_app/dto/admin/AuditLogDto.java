package com.backend.tutor_app.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO pour les logs d'audit système
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    
    private Long id;
    
    // Action effectuée
    private String action;
    private String actionType; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.
    private String entityType; // User, TutorApplication, Report, etc.
    private Long entityId;
    
    // Utilisateur qui a effectué l'action
    private Long userId;
    private String userEmail;
    private String userName;
    private String userRole;
    
    // Détails de l'action
    private String description;
    private Map<String, Object> oldValues; // Valeurs avant modification
    private Map<String, Object> newValues; // Valeurs après modification
    private String changes; // Résumé des changements
    
    // Contexte technique
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String requestId;
    
    // Métadonnées
    private LocalDateTime timestamp;
    private String severity; // INFO, WARNING, ERROR, CRITICAL
    private String module; // AUTH, USER_MANAGEMENT, MODERATION, etc.
    private Boolean success;
    private String errorMessage;
    
    // Géolocalisation (optionnel)
    private String country;
    private String city;
    private String region;
}
