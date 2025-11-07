package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.notification.NotificationDTO;
import com.backend.tutor_app.dto.notification.NotificationRequest;
import com.backend.tutor_app.model.Notification;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.NotificationPriority;
import com.backend.tutor_app.model.enums.NotificationType;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.repositories.NotificationRepository;
import com.backend.tutor_app.repositories.UserRepository;
import com.backend.tutor_app.services.EmailService;
import com.backend.tutor_app.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implémentation du service global de notifications
 * Gère WebSocket, FCM, Email et la persistance des notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Map des sessions WebSocket actives : userId -> Set<sessionId>
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // Map des tokens FCM : userId -> Set<fcmToken>
    private final Map<Long, Set<String>> userFCMTokens = new ConcurrentHashMap<>();
    
    // Map inverse pour retrouver userId depuis sessionId
    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();
    
    // ==================== ENVOI DE NOTIFICATIONS ====================
    
    @Override
    @Async
    public boolean sendWebSocketNotification(Long userId, NotificationDTO notification) {
        try {
            if (!isUserConnected(userId)) {
                log.debug("📡 User {} not connected via WebSocket, skipping", userId);
                return false;
            }
            
            // Envoi via WebSocket à destination /topic/notifications/{userId}
            messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                notification
            );
            
            log.info("📡 WebSocket notification sent to user {}: {}", userId, notification.getTitle());
            return true;
            
        } catch (Exception e) {
            log.error("❌ Error sending WebSocket notification to user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    @Override
    @Async
    public boolean sendFCMNotification(Long userId, NotificationDTO notification) {
        try {
            Set<String> tokens = userFCMTokens.get(userId);
            if (tokens == null || tokens.isEmpty()) {
                log.debug("📱 User {} has no FCM tokens registered, skipping", userId);
                return false;
            }
            
            // TODO: Implémenter l'envoi FCM réel avec Firebase Admin SDK
            // Pour l'instant, on simule l'envoi
            log.info("📱 FCM notification would be sent to user {} ({} tokens): {}", 
                userId, tokens.size(), notification.getTitle());
            
            // Simulation d'envoi réussi
            return true;
            
        } catch (Exception e) {
            log.error("❌ Error sending FCM notification to user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public NotificationDTO sendToUser(Long userId, NotificationRequest request) {
        try {
            // 1. Récupérer l'utilisateur
            Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            // 2. Créer et persister la notification
            Notification notification = Notification.builder()
                .user(user)
                .type(request.getType())
                .priority(request.getPriority())
                .title(request.getTitle())
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .actionUrl(request.getActionUrl())
                .actionLabel(request.getActionLabel())
                .iconUrl(request.getIconUrl())
                .createdAt(LocalDateTime.now())
                .build();
            
            notification = notificationRepository.save(notification);
            
            // 3. Convertir en DTO
            NotificationDTO dto = convertToDTO(notification);
            
            // 4. Envoi via différents canaux (asynchrone)
            if (request.isSendWebSocket()) {
                boolean sentWS = sendWebSocketNotification(userId, dto);
                notification.setSentViaWebSocket(sentWS);
            }
            
            if (request.isSendPush()) {
                boolean sentFCM = sendFCMNotification(userId, dto);
                notification.setSentViaFCM(sentFCM);
            }
            
            if (request.isSendEmail()) {
                sendEmailNotification(user, dto);
                notification.setSentViaEmail(true);
            }
            
            // 5. Mettre à jour les flags d'envoi
            notificationRepository.save(notification);
            
            log.info("✅ Notification sent to user {}: {} (WS:{}, FCM:{}, Email:{})",
                userId, request.getTitle(),
                notification.isSentViaWebSocket(),
                notification.isSentViaFCM(),
                notification.isSentViaEmail());
            
            return convertToDTO(notification);
            
        } catch (Exception e) {
            log.error("❌ Error sending notification to user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }
    
    @Override
    @Transactional
    public List<NotificationDTO> sendToAdmins(NotificationRequest request) {
        try {
            // Récupérer tous les administrateurs
            List<Utilisateur> admins = userRepository.findByRole(Role.ADMIN);
            
            if (admins.isEmpty()) {
                log.warn("⚠️ No admins found to send notification");
                return Collections.emptyList();
            }
            
            List<NotificationDTO> sentNotifications = new ArrayList<>();
            
            for (Utilisateur admin : admins) {
                try {
                    NotificationDTO dto = sendToUser(admin.getId(), request);
                    sentNotifications.add(dto);
                } catch (Exception e) {
                    log.error("❌ Failed to send notification to admin {}: {}", 
                        admin.getId(), e.getMessage());
                }
            }
            
            log.info("✅ Notification sent to {} admins: {}", sentNotifications.size(), request.getTitle());
            return sentNotifications;
            
        } catch (Exception e) {
            log.error("❌ Error sending notification to admins: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send notification to admins", e);
        }
    }
    
    @Override
    @Transactional
    public int sendToRole(String role, NotificationRequest request) {
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            List<Utilisateur> users = userRepository.findByRole(userRole);
            
            if (users.isEmpty()) {
                log.warn("⚠️ No users found with role {} to send notification", role);
                return 0;
            }
            
            int sentCount = 0;
            for (Utilisateur user : users) {
                try {
                    sendToUser(user.getId(), request);
                    sentCount++;
                } catch (Exception e) {
                    log.error("❌ Failed to send notification to user {}: {}", 
                        user.getId(), e.getMessage());
                }
            }
            
            log.info("✅ Notification sent to {} users with role {}: {}", 
                sentCount, role, request.getTitle());
            return sentCount;
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid role: {}", role);
            throw new RuntimeException("Invalid role: " + role);
        } catch (Exception e) {
            log.error("❌ Error sending notification to role {}: {}", role, e.getMessage(), e);
            throw new RuntimeException("Failed to send notification to role", e);
        }
    }
    
    @Override
    @Transactional
    public int broadcast(NotificationRequest request) {
        try {
            List<Utilisateur> allUsers = userRepository.findAll();
            
            if (allUsers.isEmpty()) {
                log.warn("⚠️ No users found to broadcast notification");
                return 0;
            }
            
            int sentCount = 0;
            for (Utilisateur user : allUsers) {
                try {
                    sendToUser(user.getId(), request);
                    sentCount++;
                } catch (Exception e) {
                    log.error("❌ Failed to broadcast notification to user {}: {}", 
                        user.getId(), e.getMessage());
                }
            }
            
            log.info("📢 Broadcast notification sent to {} users: {}", sentCount, request.getTitle());
            return sentCount;
            
        } catch (Exception e) {
            log.error("❌ Error broadcasting notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to broadcast notification", e);
        }
    }
    
    // ==================== GESTION DES CONNEXIONS WEBSOCKET ====================
    
    @Override
    public boolean isUserConnected(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
    
    @Override
    public List<Long> getConnectedUsers() {
        return new ArrayList<>(userSessions.keySet());
    }
    
    @Override
    public void registerUserSession(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionToUser.put(sessionId, userId);
        log.info("🔌 User {} connected via WebSocket (session: {})", userId, sessionId);
    }
    
    @Override
    public void unregisterUserSession(String sessionId) {
        Long userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.info("🔌 User {} disconnected from WebSocket (session: {})", userId, sessionId);
        }
    }
    
    // ==================== GESTION DES TOKENS FCM ====================
    
    @Override
    public void registerFCMToken(Long userId, String fcmToken) {
        userFCMTokens.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(fcmToken);
        log.info("📱 FCM token registered for user {}", userId);
    }
    
    @Override
    public void removeFCMToken(String fcmToken) {
        userFCMTokens.values().forEach(tokens -> tokens.remove(fcmToken));
        log.info("📱 FCM token removed");
    }
    
    @Override
    public List<String> getUserFCMTokens(Long userId) {
        Set<String> tokens = userFCMTokens.get(userId);
        return tokens != null ? new ArrayList<>(tokens) : Collections.emptyList();
    }
    
    // ==================== RÉCUPÉRATION DES NOTIFICATIONS ====================
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId, boolean unreadOnly) {
        Utilisateur user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        List<Notification> notifications = unreadOnly
            ? notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)
            : notificationRepository.findByUserOrderByCreatedAtDesc(user);
        
        return notifications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        return convertToDTO(notification);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        Utilisateur user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        return notificationRepository.countByUserAndReadFalse(user);
    }
    
    // ==================== MARQUAGE ET SUPPRESSION ====================
    
    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Utilisateur user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
            .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));
        
        if (!notification.isRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
            log.info("✅ Notification {} marked as read for user {}", notificationId, userId);
        }
    }
    
    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        Utilisateur user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        int count = notificationRepository.markAllAsReadForUser(user, LocalDateTime.now());
        log.info("✅ {} notifications marked as read for user {}", count, userId);
        return count;
    }
    
    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Utilisateur user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
            .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));
        
        notificationRepository.delete(notification);
        log.info("🗑️ Notification {} deleted for user {}", notificationId, userId);
    }
    
    @Override
    @Transactional
    public int deleteReadNotifications(Long userId) {
        Utilisateur user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        int count = notificationRepository.deleteReadNotificationsForUser(user);
        log.info("🗑️ {} read notifications deleted for user {}", count, userId);
        return count;
    }
    
    // ==================== NOTIFICATIONS SPÉCIALISÉES ====================
    
    @Override
    public void sendSecurityAlert(Long userId, String title, String message, Map<String, Object> metadata) {
        NotificationRequest request = NotificationRequest.builder()
            .type(NotificationType.SECURITY_ALERT)
            .priority(NotificationPriority.CRITICAL)
            .title(title)
            .message(message)
            .metadata(metadata)
            .actionUrl("/dashboard/security")
            .actionLabel("Voir les détails")
            .iconUrl("/icons/security-alert.svg")
            .sendEmail(true)  // Toujours envoyer par email pour les alertes de sécurité
            .sendPush(true)
            .sendWebSocket(true)
            .build();
        
        sendToUser(userId, request);
        log.info("🚨 Security alert sent to user {}: {}", userId, title);
    }
    
//    @Override
//    public void sendReservationNotification(Long userId, Long reservationId, NotificationType type, String message) {
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("reservationId", reservationId);
//
//        NotificationPriority priority = type == NotificationType.BOOKING_CANCELLED
//            ? NotificationPriority.HIGH
//            : NotificationPriority.MEDIUM;
//
//        NotificationRequest request = NotificationRequest.builder()
//            .type(type)
//            .priority(priority)
//            .title(type.getDescription())
//            .message(message)
//            .metadata(metadata)
//            .actionUrl("/dashboard/reservations/" + reservationId)
//            .actionLabel("Voir la réservation")
//            .iconUrl("/icons/calendar.svg")
//            .sendEmail(false)
//            .sendPush(true)
//            .sendWebSocket(true)
//            .build();
//
//        sendToUser(userId, request);
//        log.info("📅 Reservation notification sent to user {}: {}", userId, type);
//    }
    
//    @Override
//    public void sendChatNotification(Long userId, Long senderId, String senderName, String messagePreview) {
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("senderId", senderId);
//        metadata.put("senderName", senderName);
//
//        NotificationRequest request = NotificationRequest.builder()
//            .type(NotificationType.NEW_MESSAGE)
//            .priority(NotificationPriority.MEDIUM)
//            .title("Nouveau message de " + senderName)
//            .message(messagePreview)
//            .metadata(metadata)
//            .actionUrl("/dashboard/messages/" + senderId)
//            .actionLabel("Répondre")
//            .iconUrl("/icons/message.svg")
//            .sendEmail(false)
//            .sendPush(true)
//            .sendWebSocket(true)
//            .build();
//
//        sendToUser(userId, request);
//        log.info("💬 Chat notification sent to user {} from sender {}", userId, senderId);
//    }
    
    @Override
    public void sendSystemNotification(Long userId, String title, String message, NotificationPriority priority) {
        NotificationRequest request = NotificationRequest.builder()
            .type(NotificationType.SYSTEM_ANNOUNCEMENT)
            .priority(priority)
            .title(title)
            .message(message)
            .actionUrl("/dashboard")
            .iconUrl("/icons/system.svg")
            .sendEmail(priority == NotificationPriority.CRITICAL)
            .sendPush(true)
            .sendWebSocket(true)
            .build();
        
        sendToUser(userId, request);
        log.info("🔔 System notification sent to user {}: {}", userId, title);
    }
    
    // ==================== NETTOYAGE ====================
    
    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
    public int cleanupOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int count = notificationRepository.deleteOldNotifications(thirtyDaysAgo);
        log.info("🧹 Cleanup: {} old notifications deleted", count);
        return count;
    }
    
    @Override
    @Transactional
    @Scheduled(cron = "0 0 3 * * ?") // Tous les jours à 3h du matin
    public int cleanupExpiredFCMTokens() {
        // TODO: Implémenter la vérification des tokens FCM expirés avec Firebase Admin SDK
        // Pour l'instant, on ne fait rien
        log.info("🧹 FCM token cleanup scheduled (not implemented yet)");
        return 0;
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Convertit une entité Notification en DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .userId(notification.getUser().getId())
            .type(notification.getType())
            .priority(notification.getPriority())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .metadata(notification.getMetadata())
            .actionUrl(notification.getActionUrl())
            .actionLabel(notification.getActionLabel())
            .iconUrl(notification.getIconUrl())
            .read(notification.isRead())
            .createdAt(notification.getCreatedAt())
            .readAt(notification.getReadAt())
            .sentViaWebSocket(notification.isSentViaWebSocket())
            .sentViaFCM(notification.isSentViaFCM())
            .sentViaEmail(notification.isSentViaEmail())
            .build();
    }
    
    /**
     * Envoie une notification par email
     */
    @Async
    private void sendEmailNotification(Utilisateur user, NotificationDTO notification) {
        try {
            // Construire le contenu de l'email
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getFirstName() + " " + user.getLastName());
            variables.put("title", notification.getTitle());
            variables.put("message", notification.getMessage());
            variables.put("actionUrl", notification.getActionUrl());
            variables.put("actionLabel", notification.getActionLabel());
            variables.put("priority", notification.getPriority().getDescription());
            
            // Envoyer l'email via EmailService
            emailService.sendTemplatedEmail(
                user.getEmail(),
                notification.getTitle(),
                "notification-email", // Template Thymeleaf
                variables
            );
            
            log.info("📧 Email notification sent to {}: {}", user.getEmail(), notification.getTitle());
            
        } catch (Exception e) {
            log.error("❌ Failed to send email notification to {}: {}", 
                user.getEmail(), e.getMessage());
        }
    }
}
