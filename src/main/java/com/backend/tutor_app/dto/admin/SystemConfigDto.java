package com.backend.tutor_app.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO pour la configuration système
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDto {
    
    private Long id;
    
    // Configuration générale
    private String applicationName;
    private String applicationVersion;
    private String environment; // DEV, STAGING, PROD
    private Boolean maintenanceMode;
    private String maintenanceMessage;
    
    // Configuration utilisateurs
    private Integer maxLoginAttempts;
    private Integer sessionTimeoutMinutes;
    private Boolean emailVerificationRequired;
    private Boolean autoApproveUsers;
    private Integer passwordMinLength;
    private Boolean requireStrongPassword;
    
    // Configuration tuteurs
    private Boolean autoApproveTutors;
    private Integer tutorApplicationReviewDays;
    private Double minHourlyRate;
    private Double maxHourlyRate;
    private Integer maxSubjectsPerTutor;
    
    // Configuration modération
    private Boolean autoModerationEnabled;
    private Integer reportThresholdForSuspension;
    private Integer moderationResponseTimeHours;
    private Boolean notifyAdminsOnReport;
    
    // Configuration fichiers
    private Long maxFileUploadSize; // en bytes
    private String allowedFileTypes;
    private String storageProvider; // LOCAL, AWS_S3, etc.
    private Integer fileRetentionDays;
    
    // Configuration email
    private Boolean emailNotificationsEnabled;
    private String emailProvider;
    private String fromEmail;
    private String fromName;
    private Boolean emailTemplatesEnabled;
    
    // Configuration sécurité
    private Boolean rateLimitingEnabled;
    private Integer apiRateLimit; // requêtes par minute
    private Boolean corsEnabled;
    private String allowedOrigins;
    private Boolean httpsOnly;
    
    // Configuration logs
    private String logLevel; // DEBUG, INFO, WARN, ERROR
    private Integer logRetentionDays;
    private Boolean auditLogsEnabled;
    private Boolean performanceMonitoringEnabled;
    
    // Paramètres personnalisés
    private Map<String, Object> customSettings;
    
    // Métadonnées
    private LocalDateTime lastUpdated;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime createdAt;
}
