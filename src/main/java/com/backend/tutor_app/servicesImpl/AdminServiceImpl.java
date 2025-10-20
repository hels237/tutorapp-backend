package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.admin.*;
import com.backend.tutor_app.dto.Auth.UserDto;
import com.backend.tutor_app.dto.common.PagedResponse;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.repositories.UserRepository;
import com.backend.tutor_app.services.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation du service d'administration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    // ==================== STATISTIQUES ====================

    @Override
    public AdminStatsDto getAdminStatistics() {
        log.info("Récupération des statistiques administrateur");
        
        try {
            // Statistiques utilisateurs
            Long totalUsers = userRepository.count();
            Long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
            Long inactiveUsers = userRepository.countByStatus(UserStatus.INACTIVE);
            Long suspendedUsers = userRepository.countByStatus(UserStatus.SUSPENDED);
            
            // Nouveaux utilisateurs
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            
            Long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
            Long newUsersToday = userRepository.countByCreatedAtAfter(startOfDay);
            
            // Statistiques par rôle
            Long totalStudents = userRepository.countByRole(Role.STUDENT);
            Long totalTutors = userRepository.countByRole(Role.TUTOR);
            Long totalAdmins = userRepository.countByRole(Role.ADMIN);
            
            return AdminStatsDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .suspendedUsers(suspendedUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .newUsersToday(newUsersToday)
                .totalStudents(totalStudents)
                .totalTutors(totalTutors)
                .totalAdmins(totalAdmins)
                // Valeurs par défaut pour les fonctionnalités non encore implémentées
                .pendingTutorApplications(0L)
                .approvedTutorApplications(0L)
                .rejectedTutorApplications(0L)
                .pendingReports(0L)
                .resolvedReports(0L)
                .totalReports(0L)
                .totalSessions(0L)
                .activeSessions(0L)
                .systemLoad(0.0)
                .storageUsed(0L)
                .usersByMonth(new HashMap<>())
                .loginsByDay(new HashMap<>())
                .reportsByType(new HashMap<>())
                .lastUpdated(LocalDateTime.now())
                .systemVersion("1.0.0")
                .databaseVersion("PostgreSQL 15")
                .build();
                
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques admin: {}", e.getMessage());
            throw new RuntimeException("Erreur récupération statistiques", e);
        }
    }

    @Override
    public Map<String, Object> getDetailedAdminStats(int days) {
        log.info("Récupération des statistiques détaillées pour {} jours", days);
        
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        try {
            // Évolution des utilisateurs
            List<Utilisateur> recentUtilisateurs = userRepository.findByCreatedAtAfter(startDate);
            Map<String, Long> usersByDay = recentUtilisateurs.stream()
                .collect(Collectors.groupingBy(
                    user -> user.getCreatedAt().toLocalDate().toString(),
                    Collectors.counting()
                ));
            
            stats.put("usersByDay", usersByDay);
            stats.put("totalNewUsers", (long) recentUtilisateurs.size());
            stats.put("period", days + " jours");
            stats.put("startDate", startDate);
            stats.put("endDate", LocalDateTime.now());
            
            return stats;
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques détaillées: {}", e.getMessage());
            throw new RuntimeException("Erreur récupération statistiques détaillées", e);
        }
    }

    // ==================== GESTION UTILISATEURS ====================

    @Override
    public PagedResponse<UserDto> getAllUsersForAdmin(String search, String role, String status, 
            Boolean verified, String startDate, String endDate, Pageable pageable) {
        
        log.info("Récupération des utilisateurs pour admin avec filtres");
        
        try {
            // Pour l'instant, récupération simple - à améliorer avec des critères de recherche
            Page<Utilisateur> users = userRepository.findAll(pageable);
            
            List<UserDto> userDtos = users.getContent().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
            
            return PagedResponse.<UserDto>builder()
                .content(userDtos)
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
                
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs admin: {}", e.getMessage());
            throw new RuntimeException("Erreur récupération utilisateurs", e);
        }
    }

    @Override
    public UserDto getUserDetailsForAdmin(Long userId) {
        log.info("Récupération des détails utilisateur {} pour admin", userId);
        
        Utilisateur utilisateur = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        return convertToUserDto(utilisateur);
    }

    @Override
    public UserDto updateUserStatusByAdmin(Long userId, String status, String reason) {
        log.info("Mise à jour du statut utilisateur {} vers {} par admin", userId, status);
        
        Utilisateur utilisateur = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        try {
            UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
            utilisateur.setStatus(userStatus);
            utilisateur.setLastUpdate(LocalDateTime.now());
            
            Utilisateur updatedUtilisateur = userRepository.save(utilisateur);
            
            // Log de l'action admin
            logAdminAction("UPDATE_USER_STATUS", userId, 
                "Statut changé vers " + status + ". Raison: " + reason, 
                Map.of("oldStatus", utilisateur.getStatus(), "newStatus", userStatus, "reason", reason));
            
            return convertToUserDto(updatedUtilisateur);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide: " + status);
        }
    }

    @Override
    public UserDto updateUserRoleByAdmin(Long userId, String role) {
        log.info("Mise à jour du rôle utilisateur {} vers {} par admin", userId, role);
        
        Utilisateur utilisateur = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            Role oldRole = utilisateur.getRole();
            utilisateur.setRole(userRole);
            utilisateur.setLastUpdate(LocalDateTime.now());
            
            Utilisateur updatedUtilisateur = userRepository.save(utilisateur);
            
            // Log de l'action admin
            logAdminAction("UPDATE_USER_ROLE", userId, 
                "Rôle changé de " + oldRole + " vers " + role, 
                Map.of("oldRole", oldRole, "newRole", userRole));
            
            return convertToUserDto(updatedUtilisateur);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + role);
        }
    }

    @Override
    public void deleteUserByAdmin(Long userId, String reason) {
        log.warn("Suppression définitive de l'utilisateur {} par admin. Raison: {}", userId, reason);
        
        Utilisateur utilisateur = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Log avant suppression
        logAdminAction("DELETE_USER", userId, 
            "Utilisateur supprimé définitivement. Raison: " + reason, 
            Map.of("userEmail", utilisateur.getEmail(), "userName", utilisateur.getFirstName() + " " + utilisateur.getLastName()));
        
        userRepository.delete(utilisateur);
    }

    // ==================== FONCTIONNALITÉS NON IMPLÉMENTÉES (STUBS) ====================

    @Override
    public PagedResponse<TutorApplicationDto> getTutorApplications(String status, Pageable pageable) {
        log.info("Récupération des demandes de tuteur - fonctionnalité non implémentée");
        
        // Stub - retourne une liste vide
        return PagedResponse.<TutorApplicationDto>builder()
            .content(new ArrayList<>())
            .page(0)
            .size(0)
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .build();
    }

    @Override
    public TutorApplicationDto getTutorApplicationDetails(Long applicationId) {
        log.info("Récupération détails demande tuteur {} - fonctionnalité non implémentée", applicationId);
        throw new RuntimeException("Fonctionnalité non implémentée");
    }

    @Override
    public TutorApplicationDto approveTutorApplication(Long applicationId, String comment) {
        log.info("Approbation demande tuteur {} - fonctionnalité non implémentée", applicationId);
        throw new RuntimeException("Fonctionnalité non implémentée");
    }

    @Override
    public TutorApplicationDto rejectTutorApplication(Long applicationId, String reason) {
        log.info("Rejet demande tuteur {} - fonctionnalité non implémentée", applicationId);
        throw new RuntimeException("Fonctionnalité non implémentée");
    }

    @Override
    public PagedResponse<UserModerationDto> getModerationReports(String type, String status, Pageable pageable) {
        log.info("Récupération signalements modération - fonctionnalité non implémentée");
        
        // Stub - retourne une liste vide
        return PagedResponse.<UserModerationDto>builder()
            .content(new ArrayList<>())
            .page(0)
            .size(0)
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .build();
    }

    @Override
    public UserModerationDto resolveModerationReport(Long reportId, String action, String comment) {
        log.info("Résolution signalement {} - fonctionnalité non implémentée", reportId);
        throw new RuntimeException("Fonctionnalité non implémentée");
    }

    @Override
    public SystemConfigDto getSystemConfiguration() {
        log.info("Récupération configuration système - fonctionnalité non implémentée");
        
        // Stub - retourne une configuration par défaut
        return SystemConfigDto.builder()
            .applicationName("TutorApp")
            .applicationVersion("1.0.0")
            .environment("DEV")
            .maintenanceMode(false)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    @Override
    public SystemConfigDto updateSystemConfiguration(Map<String, Object> config) {
        log.info("Mise à jour configuration système - fonctionnalité non implémentée");
        throw new RuntimeException("Fonctionnalité non implémentée");
    }

    @Override
    public PagedResponse<AuditLogDto> getAuditLogs(String action, Long userId, String startDate, String endDate, Pageable pageable) {
        log.info("Récupération logs audit - fonctionnalité non implémentée");
        
        // Stub - retourne une liste vide
        return PagedResponse.<AuditLogDto>builder()
            .content(new ArrayList<>())
            .page(0)
            .size(0)
            .totalElements(0L)
            .totalPages(0)
            .first(true)
            .last(true)
            .build();
    }

    @Override
    public void logAdminAction(String action, Long userId, String details, Map<String, Object> metadata) {
        log.info("Log action admin: {} pour utilisateur {} - {}", action, userId, details);
        // Pour l'instant, juste un log - à implémenter avec une vraie table d'audit
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private UserDto convertToUserDto(Utilisateur utilisateur) {
        return UserDto.builder()
            .id(utilisateur.getId())
            .email(utilisateur.getEmail())
            .firstName(utilisateur.getFirstName())
            .lastName(utilisateur.getLastName())
            .role(utilisateur.getRole().name())
            .status(utilisateur.getStatus().name())
            .emailVerified(utilisateur.getEmailVerified())
            .profilePicture(utilisateur.getProfilePicture())
            .phoneNumber(utilisateur.getPhoneNumber())
            .createdAt(utilisateur.getCreatedAt())
            .updatedAt(utilisateur.getLastUpdate())
            .lastLogin(utilisateur.getLastLogin())
            .build();
    }
}
