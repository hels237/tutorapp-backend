package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.services.AttackPatternDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PHASE 4 : Implémentation de la détection de patterns d'attaque
 * Utilise un cache en mémoire pour tracker les tentatives suspectes
 */
@Service
@Slf4j
public class AttackPatternDetectionServiceImpl implements AttackPatternDetectionService {
    
    // Cache en mémoire : userId -> Liste de tentatives avec timestamp
    private final Map<Long, List<SuspiciousAttempt>> attemptsCache = new ConcurrentHashMap<>();
    
    // Seuil de détection : 3 tentatives en 15 minutes
    private static final int ATTEMPT_THRESHOLD = 3;
    private static final int TIME_WINDOW_MINUTES = 15;
    
    @Override
    public void recordSuspiciousAttempt(Long userId, String reason) {
        log.warn("[PHASE 4][WARNING] Tentative suspecte enregistrée - UserID: {}, Raison: {}", userId, reason);
        
        attemptsCache.computeIfAbsent(userId, k -> new ArrayList<>())
            .add(new SuspiciousAttempt(LocalDateTime.now(), reason));
        
        // Nettoyer les anciennes tentatives
        cleanupOldAttempts(userId);
        
        // Vérifier si pattern d'attaque
        if (hasAttackPattern(userId)) {
            log.error("[PHASE 4][CRITICAL] Pattern d'attaque détecté - UserID: {}, Tentatives: {}", 
                userId, getRecentSuspiciousAttempts(userId));
        }
    }
    
    @Override
    public boolean hasAttackPattern(Long userId) {
        int recentAttempts = getRecentSuspiciousAttempts(userId);
        return recentAttempts >= ATTEMPT_THRESHOLD;
    }
    
    @Override
    public int getRecentSuspiciousAttempts(Long userId) {
        List<SuspiciousAttempt> attempts = attemptsCache.get(userId);
        if (attempts == null || attempts.isEmpty()) {
            return 0;
        }
        
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);
        return (int) attempts.stream()
            .filter(attempt -> attempt.timestamp.isAfter(cutoff))
            .count();
    }
    
    @Override
    public void resetAttempts(Long userId) {
        attemptsCache.remove(userId);
        log.info("[PHASE 4] Compteur tentatives réinitialisé - UserID: {}", userId);
    }
    
    /**
     * Nettoie les tentatives plus anciennes que la fenêtre de temps
     */
    private void cleanupOldAttempts(Long userId) {
        List<SuspiciousAttempt> attempts = attemptsCache.get(userId);
        if (attempts == null) {
            return;
        }
        
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);
        attempts.removeIf(attempt -> attempt.timestamp.isBefore(cutoff));
        
        // Supprimer l'entrée si plus d'attempts
        if (attempts.isEmpty()) {
            attemptsCache.remove(userId);
        }
    }
    
    /**
     * Classe interne pour stocker une tentative suspecte
     */
    private static class SuspiciousAttempt {
        private final LocalDateTime timestamp;
        private final String reason;
        
        public SuspiciousAttempt(LocalDateTime timestamp, String reason) {
            this.timestamp = timestamp;
            this.reason = reason;
        }
    }
}
