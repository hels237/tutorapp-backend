package com.backend.tutor_app.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO pour les statistiques administrateur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {
    
    // Statistiques utilisateurs
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long suspendedUsers;
    private Long newUsersThisMonth;
    private Long newUsersToday;
    
    // Statistiques par rôle
    private Long totalStudents;
    private Long totalTutors;
    private Long totalAdmins;
    
    // Statistiques tuteurs
    private Long pendingTutorApplications;
    private Long approvedTutorApplications;
    private Long rejectedTutorApplications;
    
    // Statistiques modération
    private Long pendingReports;
    private Long resolvedReports;
    private Long totalReports;
    
    // Statistiques système
    private Long totalSessions;
    private Long activeSessions;
    private Double systemLoad;
    private Long storageUsed; // en bytes
    
    // Statistiques par période
    private Map<String, Long> usersByMonth;
    private Map<String, Long> loginsByDay;
    private Map<String, Long> reportsByType;
    
    // Métadonnées
    private LocalDateTime lastUpdated;
    private String systemVersion;
    private String databaseVersion;
}
