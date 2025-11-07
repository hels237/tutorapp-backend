package com.backend.tutor_app.controller;

import com.backend.tutor_app.dto.common.ApiResponseDto;
import com.backend.tutor_app.dto.notification.NotificationDTO;
import com.backend.tutor_app.dto.notification.NotificationRequest;
import com.backend.tutor_app.services.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    // ==================== RÉCUPÉRATION DES NOTIFICATIONS ====================
    
    /**
     * Récupère toutes les notifications de l'utilisateur connecté
     * GET /api/notifications?unreadOnly=true
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<List<NotificationDTO>>> getUserNotifications(
        @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
        @RequestAttribute("userId") Long userId
    ) {
        log.info("📥 GET /api/notifications - userId: {}, unreadOnly: {}", userId, unreadOnly);
        
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId, unreadOnly);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            notifications,
            notifications.size() + " notification(s) récupérée(s)"
        ));
    }
    
    /**
     * Récupère une notification par son ID
     * GET /api/notifications/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<NotificationDTO>> getNotificationById(
        @PathVariable Long id,
        @RequestAttribute("userId") Long userId
    ) {
        log.info("📥 GET /api/notifications/{} - userId: {}", id, userId);
        
        NotificationDTO notification = notificationService.getNotificationById(id);
        
        // Vérifier que la notification appartient à l'utilisateur
        if (!notification.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(ApiResponseDto.error(
                "Accès refusé à cette notification"+
                "FORBIDDEN"
            ));
        }
        
        return ResponseEntity.ok(ApiResponseDto.success(
            notification,
            "Notification récupérée avec succès"
        ));
    }
    
    /**
     * Compte le nombre de notifications non lues
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Integer>> getUnreadCount(
        @RequestAttribute("userId") Long userId
    ) {
        log.info(" GET /api/notifications/unread/count - userId: {}", userId);
        
        int count = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            count,
            count + " notification(s) non lue(s)"
        ));
    }
    
    // ==================== MARQUAGE DES NOTIFICATIONS ====================
    
    /**
     * Marque une notification comme lue
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Void>> markAsRead(
        @PathVariable Long id,
        @RequestAttribute("userId") Long userId
    ) {
        log.info("✅ PUT /api/notifications/{}/read - userId: {}", id, userId);
        
        notificationService.markAsRead(id, userId);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            null,
            "Notification marquée comme lue"
        ));
    }
    
    /**
     * Marque toutes les notifications comme lues
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Integer>> markAllAsRead(
        @RequestAttribute("userId") Long userId
    ) {
        log.info("✅ PUT /api/notifications/read-all - userId: {}", userId);
        
        int count = notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            count,
            count + " notification(s) marquée(s) comme lue(s)"
        ));
    }
    
    // ==================== SUPPRESSION DES NOTIFICATIONS ====================
    
    /**
     * Supprime une notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Void>> deleteNotification(
        @PathVariable Long id,
        @RequestAttribute("userId") Long userId
    ) {
        log.info(" DELETE /api/notifications/{} - userId: {}", id, userId);
        
        notificationService.deleteNotification(id, userId);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            null,
            "Notification supprimée avec succès"
        ));
    }
    
    /**
     * Supprime toutes les notifications lues
     * DELETE /api/notifications/read
     */
    @DeleteMapping("/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Integer>> deleteReadNotifications(
        @RequestAttribute("userId") Long userId
    ) {
        log.info(" DELETE /api/notifications/read - userId: {}", userId);
        
        int count = notificationService.deleteReadNotifications(userId);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            count,
            count + " notification(s) supprimée(s)"
        ));
    }
    
    // ==================== ENVOI DE NOTIFICATIONS (ADMIN UNIQUEMENT) ====================
    
    /**
     * Envoie une notification à un utilisateur spécifique
     * POST /api/notifications/send/{userId}
     */
    @PostMapping("/send/{targetUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<NotificationDTO>> sendToUser(
        @PathVariable Long targetUserId,
        @Valid @RequestBody NotificationRequest request
    ) {
        log.info("📤 POST /api/notifications/send/{} - Admin sending notification", targetUserId);
        
        NotificationDTO notification = notificationService.sendToUser(targetUserId, request);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            notification,
            "Notification envoyée avec succès"
        ));
    }
    
    /**
     * Envoie une notification à tous les administrateurs
     * POST /api/notifications/send/admins
     */
    @PostMapping("/send/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<List<NotificationDTO>>> sendToAdmins(
        @Valid @RequestBody NotificationRequest request
    ) {
        log.info("📤 POST /api/notifications/send/admins - Broadcasting to admins");
        
        List<NotificationDTO> notifications = notificationService.sendToAdmins(request);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            notifications,
            notifications.size() + " notification(s) envoyée(s) aux admins"
        ));
    }
    
    /**
     * Envoie une notification à un rôle spécifique
     * POST /api/notifications/send/role/{role}
     */
    @PostMapping("/send/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Integer>> sendToRole(
        @PathVariable String role,
        @Valid @RequestBody NotificationRequest request
    ) {
        log.info("📤 POST /api/notifications/send/role/{} - Broadcasting to role", role);
        
        int count = notificationService.sendToRole(role, request);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            count,
            count + " notification(s) envoyée(s) au rôle " + role
        ));
    }
    
    /**
     * Broadcast une notification à tous les utilisateurs
     * POST /api/notifications/broadcast
     */
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Integer>> broadcast(
        @Valid @RequestBody NotificationRequest request
    ) {
        log.info("📢 POST /api/notifications/broadcast - Broadcasting to all users");
        
        int count = notificationService.broadcast(request);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            count,
            count + " notification(s) envoyée(s) à tous les utilisateurs"
        ));
    }
    
    // ==================== GESTION DES TOKENS FCM ====================
    
    /**
     * Enregistre un token FCM pour l'utilisateur connecté
     * POST /api/notifications/fcm/register
     */
    @PostMapping("/fcm/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Void>> registerFCMToken(
        @RequestParam String token,
        @RequestAttribute("userId") Long userId
    ) {
        log.info("📱 POST /api/notifications/fcm/register - userId: {}", userId);
        
        notificationService.registerFCMToken(userId, token);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            null,
            "Token FCM enregistré avec succès"
        ));
    }
    
    /**
     * Supprime un token FCM
     * DELETE /api/notifications/fcm/remove
     */
    @DeleteMapping("/fcm/remove")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<Void>> removeFCMToken(
        @RequestParam String token
    ) {
        log.info("📱 DELETE /api/notifications/fcm/remove");
        
        notificationService.removeFCMToken(token);
        
        return ResponseEntity.ok(ApiResponseDto.success(
            null,
            "Token FCM supprimé avec succès"
        ));
    }
}
