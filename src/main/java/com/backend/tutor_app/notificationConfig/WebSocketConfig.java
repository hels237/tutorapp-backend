package com.backend.tutor_app.notificationConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour les notifications en temps réel
 * Utilise STOMP (Simple Text Oriented Messaging Protocol) sur WebSocket
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     *  /ws : Point d’entrée pour la connexion WebSocket.
     *  enableSimpleBroker("/topic"): Active la diffusion en temps réel vers les clients abonnés a des destination.
     *  /app : Préfixe pour les messages envoyés vers le serveur
     *  /user : Permet d’envoyer des messages à un seul utilisateur (privés)
     *
     * Configure le message broker
     * - /topic : pour les messages broadcast (1 → N)
     * - /queue : pour les messages point-to-point (1 → 1)
     * - /app : préfixe pour les messages envoyés par les clients
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active un simple message broker en mémoire
        // Les destinations commençant par /topic et /queue seront gérées par le broker
        config.enableSimpleBroker("/topic", "/queue");
        
        // Préfixe pour les messages envoyés depuis les clients vers le serveur
        config.setApplicationDestinationPrefixes("/app");
        
        // Préfixe pour les messages envoyés à un utilisateur spécifique
        config.setUserDestinationPrefix("/user");
    }
    
    /**
     * Enregistre les endpoints WebSocket
     * Les clients se connecteront à ces endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint principal pour les notifications
        registry.addEndpoint("/ws/notifications")
            .setAllowedOriginPatterns("*") // À restreindre en production
            .withSockJS(); // Fallback pour les navigateurs ne supportant pas WebSocket
        
        // Endpoint pour le chat (futur)
        registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();
        
        // Endpoint pour les mises à jour en temps réel (dashboard, etc.)
        registry.addEndpoint("/ws/updates")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
