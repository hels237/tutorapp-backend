package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.admin.*;
import com.backend.tutor_app.dto.Auth.UserDto;
import com.backend.tutor_app.dto.common.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Service pour les fonctionnalités d'administration
 */
public interface AdminService {

    // ==================== STATISTIQUES ====================
    
    /**
     * Récupère les statistiques générales pour le dashboard admin
     */
    AdminStatsDto getAdminStatistics();
    
    /**
     * Récupère les statistiques détaillées sur une période
     */
    Map<String, Object> getDetailedAdminStats(int days);

    // ==================== GESTION UTILISATEURS ====================
    
    /**
     * Récupère tous les utilisateurs avec filtres pour l'administration
     */
    PagedResponse<UserDto> getAllUsersForAdmin(
        String search, String role, String status, Boolean verified, 
        String startDate, String endDate, Pageable pageable
    );
    
    /**
     * Récupère les détails complets d'un utilisateur pour l'admin
     */
    UserDto getUserDetailsForAdmin(Long userId);
    
    /**
     * Met à jour le statut d'un utilisateur (admin uniquement)
     */
    UserDto updateUserStatusByAdmin(Long userId, String status, String reason);
    
    /**
     * Met à jour le rôle d'un utilisateur (admin uniquement)
     */
    UserDto updateUserRoleByAdmin(Long userId, String role);
    
    /**
     * Supprime définitivement un utilisateur (admin uniquement)
     */
    void deleteUserByAdmin(Long userId, String reason);

    // ==================== GESTION DEMANDES TUTEUR ====================
    
    /**
     * Récupère les demandes de tuteur avec filtres
     */
    PagedResponse<TutorApplicationDto> getTutorApplications(String status, Pageable pageable);
    
    /**
     * Récupère les détails d'une demande de tuteur
     */
    TutorApplicationDto getTutorApplicationDetails(Long applicationId);
    
    /**
     * Approuve une demande de tuteur
     */
    TutorApplicationDto approveTutorApplication(Long applicationId, String comment);
    
    /**
     * Rejette une demande de tuteur
     */
    TutorApplicationDto rejectTutorApplication(Long applicationId, String reason);

    // ==================== MODÉRATION ====================
    
    /**
     * Récupère les signalements de modération
     */
    PagedResponse<UserModerationDto> getModerationReports(String type, String status, Pageable pageable);
    
    /**
     * Résout un signalement de modération
     */
    UserModerationDto resolveModerationReport(Long reportId, String action, String comment);

    // ==================== CONFIGURATION SYSTÈME ====================
    
    /**
     * Récupère la configuration système actuelle
     */
    SystemConfigDto getSystemConfiguration();
    
    /**
     * Met à jour la configuration système
     */
    SystemConfigDto updateSystemConfiguration(Map<String, Object> config);

    // ==================== LOGS D'AUDIT ====================
    
    /**
     * Récupère les logs d'audit avec filtres
     */
    PagedResponse<AuditLogDto> getAuditLogs(
        String action, Long userId, String startDate, String endDate, Pageable pageable
    );
    
    /**
     * Enregistre une action dans les logs d'audit
     */
    void logAdminAction(String action, Long userId, String details, Map<String, Object> metadata);
}
