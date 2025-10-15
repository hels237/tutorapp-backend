package com.backend.tutor_app.controller;

import com.backend.tutor_app.dto.Auth.UserDto;
import com.backend.tutor_app.dto.common.ApiResponseDto;
import com.backend.tutor_app.dto.common.PagedResponse;
import com.backend.tutor_app.dto.admin.AdminStatsDto;
import com.backend.tutor_app.dto.admin.TutorApplicationDto;
import com.backend.tutor_app.dto.admin.UserModerationDto;
import com.backend.tutor_app.services.AdminService;
import com.backend.tutor_app.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST pour l'administration TutorApp
 * Accès restreint aux administrateurs uniquement
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration", description = "Endpoints d'administration - Accès ADMIN uniquement")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final TokenService tokenService;

    // ==================== DASHBOARD ADMIN ====================

    /**
     * GET /api/v1/admin/dashboard
     * Dashboard administrateur avec statistiques globales
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard admin", description = "Récupère les statistiques globales pour le dashboard admin")
    public ResponseEntity<?> getAdminDashboard() {
        try {
            AdminStatsDto adminStats = adminService.getAdminStatistics();
            
            return ResponseEntity.ok(ApiResponseDto.success(adminStats, "Statistiques administrateur"));

        } catch (Exception e) {
            log.error("Erreur récupération dashboard admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erreur récupération statistiques"));
        }
    }

    /**
     * GET /api/v1/admin/stats/overview
     * Vue d'ensemble des statistiques détaillées
     */
    @GetMapping("/stats/overview")
    @Operation(summary = "Statistiques détaillées", description = "Statistiques détaillées de la plateforme")
    public ResponseEntity<?> getDetailedStats(
            @Parameter(description = "Période") @RequestParam(defaultValue = "30") int days) {
        
        try {
            var detailedStats = adminService.getDetailedAdminStats(days);
            
            return ResponseEntity.ok(ApiResponseDto.success(detailedStats, "Statistiques détaillées"));

        } catch (Exception e) {
            log.error("Erreur récupération statistiques détaillées: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erreur récupération statistiques"));
        }
    }

    // ==================== GESTION UTILISATEURS ====================

    /**
     * GET /api/v1/admin/users
     * Liste complète des utilisateurs avec filtres admin
     */
    @GetMapping("/users")
    @Operation(summary = "Liste utilisateurs admin", description = "Liste complète des utilisateurs pour l'administration")
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "Recherche") @RequestParam(required = false) String search,
            @Parameter(description = "Rôle") @RequestParam(required = false) String role,
            @Parameter(description = "Statut") @RequestParam(required = false) String status,
            @Parameter(description = "Vérifié") @RequestParam(required = false) Boolean verified,
            @Parameter(description = "Date début") @RequestParam(required = false) String startDate,
            @Parameter(description = "Date fin") @RequestParam(required = false) String endDate,
            @Parameter(description = "Page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Tri") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            PagedResponse<UserDto> users = adminService.getAllUsersForAdmin(
                search, role, status, verified, startDate, endDate, pageable
            );
            
            return ResponseEntity.ok(ApiResponseDto.success(users, "Utilisateurs récupérés"));

        } catch (Exception e) {
            log.error("Erreur récupération utilisateurs admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur récupération utilisateurs"));
        }
    }

    /**
     * GET /api/v1/admin/users/{id}
     * Détails complets d'un utilisateur pour l'admin
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Détails utilisateur admin", description = "Récupère tous les détails d'un utilisateur")
    public ResponseEntity<?> getUserDetails(@Parameter(description = "ID utilisateur") @PathVariable Long id) {
        try {
            UserDto userDetails = adminService.getUserDetailsForAdmin(id);
            
            return ResponseEntity.ok(ApiResponseDto.success(userDetails, "Détails utilisateur"));

        } catch (Exception e) {
            log.error("Erreur récupération détails utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error("Utilisateur non trouvé"));
        }
    }

    /**
     * PUT /api/v1/admin/users/{id}/status
     * Modification du statut d'un utilisateur
     */
    @PutMapping("/users/{id}/status")
    @Operation(summary = "Modifier statut utilisateur", description = "Change le statut d'un utilisateur")
    public ResponseEntity<?> updateUserStatus(
            @Parameter(description = "ID utilisateur") @PathVariable Long id,
            @Parameter(description = "Nouveau statut") @RequestParam String status,
            @Parameter(description = "Raison") @RequestParam(required = false) String reason) {
        
        try {
            UserDto updatedUser = adminService.updateUserStatusByAdmin(id, status, reason);
            
            return ResponseEntity.ok(ApiResponseDto.success(updatedUser, "Statut utilisateur mis à jour"));

        } catch (Exception e) {
            log.error("Erreur mise à jour statut utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur mise à jour statut"));
        }
    }

    /**
     * PUT /api/v1/admin/users/{id}/role
     * Modification du rôle d'un utilisateur
     */
    @PutMapping("/users/{id}/role")
    @Operation(summary = "Modifier rôle utilisateur", description = "Change le rôle d'un utilisateur")
    public ResponseEntity<?> updateUserRole(
            @Parameter(description = "ID utilisateur") @PathVariable Long id,
            @Parameter(description = "Nouveau rôle") @RequestParam String role) {
        
        try {
            UserDto updatedUser = adminService.updateUserRoleByAdmin(id, role);
            
            return ResponseEntity.ok(ApiResponseDto.success(updatedUser, "Rôle utilisateur mis à jour"));

        } catch (Exception e) {
            log.error("Erreur mise à jour rôle utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur mise à jour rôle"));
        }
    }

    /**
     * DELETE /api/v1/admin/users/{id}
     * Suppression d'un utilisateur par l'admin
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Supprimer utilisateur", description = "Supprime définitivement un utilisateur")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID utilisateur") @PathVariable Long id,
            @Parameter(description = "Raison") @RequestParam String reason) {
        
        try {
            adminService.deleteUserByAdmin(id, reason);
            
            return ResponseEntity.ok(ApiResponseDto.success(null, "Utilisateur supprimé définitivement"));

        } catch (Exception e) {
            log.error("Erreur suppression utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur suppression utilisateur"));
        }
    }

    // ==================== GESTION DEMANDES TUTEUR ====================

    /**
     * GET /api/v1/admin/tutor-applications
     * Liste des demandes de tuteur en attente
     */
    @GetMapping("/tutor-applications")
    @Operation(summary = "Demandes tuteur", description = "Liste des demandes de tuteur à traiter")
    public ResponseEntity<?> getTutorApplications(
            @Parameter(description = "Statut") @RequestParam(defaultValue = "PENDING") String status,
            @Parameter(description = "Page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille") @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            
            PagedResponse<TutorApplicationDto> applications = adminService.getTutorApplications(status, pageable);
            
            return ResponseEntity.ok(ApiResponseDto.success(applications, "Demandes de tuteur"));

        } catch (Exception e) {
            log.error("Erreur récupération demandes tuteur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur récupération demandes"));
        }
    }

    /**
     * GET /api/v1/admin/tutor-applications/{id}
     * Détails d'une demande de tuteur
     */
    @GetMapping("/tutor-applications/{id}")
    @Operation(summary = "Détails demande tuteur", description = "Récupère les détails d'une demande de tuteur")
    public ResponseEntity<?> getTutorApplicationDetails(@Parameter(description = "ID demande") @PathVariable Long id) {
        try {
            TutorApplicationDto applicationDetails = adminService.getTutorApplicationDetails(id);
            
            return ResponseEntity.ok(ApiResponseDto.success(applicationDetails, "Détails de la demande"));

        } catch (Exception e) {
            log.error("Erreur récupération détails demande: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error("Demande non trouvée"));
        }
    }

    /**
     * PUT /api/v1/admin/tutor-applications/{id}/approve
     * Approuver une demande de tuteur
     */
    @PutMapping("/tutor-applications/{id}/approve")
    @Operation(summary = "Approuver demande tuteur", description = "Approuve une demande de tuteur")
    public ResponseEntity<?> approveTutorApplication(
            @Parameter(description = "ID demande") @PathVariable Long id,
            @Parameter(description = "Commentaire") @RequestParam(required = false) String comment) {
        
        try {
            TutorApplicationDto approvedApplication = adminService.approveTutorApplication(id, comment);
            
            return ResponseEntity.ok(ApiResponseDto.success(approvedApplication, "Demande approuvée"));

        } catch (Exception e) {
            log.error("Erreur approbation demande tuteur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur approbation demande"));
        }
    }

    /**
     * PUT /api/v1/admin/tutor-applications/{id}/reject
     * Rejeter une demande de tuteur
     */
    @PutMapping("/tutor-applications/{id}/reject")
    @Operation(summary = "Rejeter demande tuteur", description = "Rejette une demande de tuteur")
    public ResponseEntity<?> rejectTutorApplication(
            @Parameter(description = "ID demande") @PathVariable Long id,
            @Parameter(description = "Raison du rejet") @RequestParam String reason) {
        
        try {
            TutorApplicationDto rejectedApplication = adminService.rejectTutorApplication(id, reason);
            
            return ResponseEntity.ok(ApiResponseDto.success(rejectedApplication, "Demande rejetée"));

        } catch (Exception e) {
            log.error("Erreur rejet demande tuteur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur rejet demande"));
        }
    }

    // ==================== MODÉRATION CONTENU ====================

    /**
     * GET /api/v1/admin/moderation/reports
     * Liste des signalements à traiter
     */
    @GetMapping("/moderation/reports")
    @Operation(summary = "Signalements", description = "Liste des signalements à modérer")
    public ResponseEntity<?> getModerationReports(
            @Parameter(description = "Type") @RequestParam(required = false) String type,
            @Parameter(description = "Statut") @RequestParam(defaultValue = "PENDING") String status,
            @Parameter(description = "Page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille") @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            PagedResponse<UserModerationDto> reports = adminService.getModerationReports(type, status, pageable);
            
            return ResponseEntity.ok(ApiResponseDto.success(reports, "Signalements récupérés"));

        } catch (Exception e) {
            log.error("Erreur récupération signalements: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur récupération signalements"));
        }
    }

    /**
     * PUT /api/v1/admin/moderation/reports/{id}/resolve
     * Résoudre un signalement
     */
    @PutMapping("/moderation/reports/{id}/resolve")
    @Operation(summary = "Résoudre signalement", description = "Marque un signalement comme résolu")
    public ResponseEntity<?> resolveModerationReport(
            @Parameter(description = "ID signalement") @PathVariable Long id,
            @Parameter(description = "Action prise") @RequestParam String action,
            @Parameter(description = "Commentaire") @RequestParam(required = false) String comment) {
        
        try {
            UserModerationDto resolvedReport = adminService.resolveModerationReport(id, action, comment);
            
            return ResponseEntity.ok(ApiResponseDto.success(resolvedReport, "Signalement résolu"));

        } catch (Exception e) {
            log.error("Erreur résolution signalement: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur résolution signalement"));
        }
    }

    // ==================== CONFIGURATION SYSTÈME ====================

    /**
     * GET /api/v1/admin/system/config
     * Configuration système actuelle
     */
    @GetMapping("/system/config")
    @Operation(summary = "Configuration système", description = "Récupère la configuration système")
    public ResponseEntity<?> getSystemConfig() {
        try {
            var systemConfig = adminService.getSystemConfiguration();
            
            return ResponseEntity.ok(ApiResponseDto.success(systemConfig, "Configuration système"));

        } catch (Exception e) {
            log.error("Erreur récupération configuration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erreur récupération configuration"));
        }
    }

    /**
     * PUT /api/v1/admin/system/config
     * Mise à jour de la configuration système
     */
    @PutMapping("/system/config")
    @Operation(summary = "Modifier configuration", description = "Met à jour la configuration système")
    public ResponseEntity<?> updateSystemConfig(@RequestBody Map<String, Object> config) {
        try {
            var updatedConfig = adminService.updateSystemConfiguration(config);
            
            return ResponseEntity.ok(ApiResponseDto.success(updatedConfig, "Configuration mise à jour"));

        } catch (Exception e) {
            log.error("Erreur mise à jour configuration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur mise à jour configuration"));
        }
    }

    // ==================== LOGS ET AUDIT ====================

    /**
     * GET /api/v1/admin/audit/logs
     * Logs d'audit système
     */
    @GetMapping("/audit/logs")
    @Operation(summary = "Logs audit", description = "Récupère les logs d'audit du système")
    public ResponseEntity<?> getAuditLogs(
            @Parameter(description = "Action") @RequestParam(required = false) String action,
            @Parameter(description = "Utilisateur") @RequestParam(required = false) Long userId,
            @Parameter(description = "Date début") @RequestParam(required = false) String startDate,
            @Parameter(description = "Date fin") @RequestParam(required = false) String endDate,
            @Parameter(description = "Page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille") @RequestParam(defaultValue = "50") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
            
            var auditLogs = adminService.getAuditLogs(action, userId, startDate, endDate, pageable);
            
            return ResponseEntity.ok(ApiResponseDto.success(auditLogs, "Logs d'audit"));

        } catch (Exception e) {
            log.error("Erreur récupération logs audit: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur récupération logs"));
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Extrait l'ID utilisateur du token JWT
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return tokenService.getUserIdFromJwtToken(token);
        }
        throw new IllegalArgumentException("Token manquant ou format invalide");
    }
}
