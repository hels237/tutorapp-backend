package com.backend.tutor_app.websocket;

import com.backend.tutor_app.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Gestionnaire d'événements WebSocket
 * Gère les connexions et déconnexions des utilisateurs
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    
    private final NotificationService notificationService;
    
    /**
     * Événement déclenché quand un utilisateur se connecte via WebSocket
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Récupérer l'userId depuis les headers (doit être envoyé par le client)
        String userIdStr = headerAccessor.getFirstNativeHeader("userId");
        
        if (userIdStr != null && sessionId != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                notificationService.registerUserSession(userId, sessionId);
                log.info("🔌 WebSocket connected: userId={}, sessionId={}", userId, sessionId);
            } catch (NumberFormatException e) {
                log.error("❌ Invalid userId in WebSocket connection: {}", userIdStr);
            }
        } else {
            log.warn("⚠️ WebSocket connection without userId header");
        }
    }
    
    /**
     * Événement déclenché quand un utilisateur se déconnecte
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        if (sessionId != null) {
            notificationService.unregisterUserSession(sessionId);
            log.info("🔌 WebSocket disconnected: sessionId={}", sessionId);
        }
    }
}
