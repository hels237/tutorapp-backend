package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.services.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implémentation du service de limitation de taux (Rate Limiting) pour TutorApp
 * Protège contre les abus et attaques par déni de service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    // Cache en mémoire pour les tentatives (en production, utiliser Redis)
    private final Map<String, List<LocalDateTime>> attemptCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blacklistCache = new ConcurrentHashMap<>();
    private final Set<String> whitelistCache = ConcurrentHashMap.newKeySet();
    private final Map<String, RateLimitConfig> configCache = new ConcurrentHashMap<>();

    // Configuration par défaut
    private static final Map<String, RateLimitConfig> DEFAULT_CONFIGS = Map.of(
        "login", new RateLimitConfig(5, Duration.ofMinutes(15)),
        "register", new RateLimitConfig(3, Duration.ofHours(1)),
        "password_reset", new RateLimitConfig(3, Duration.ofHours(1)),
        "email_verification", new RateLimitConfig(5, Duration.ofMinutes(30)),
        "social_auth", new RateLimitConfig(10, Duration.ofMinutes(15)),
        "api_call", new RateLimitConfig(100, Duration.ofMinutes(1))
    );

    // ==================== GENERAL RATE LIMITING ====================

    @Override
    public boolean isAllowed(String key, String action) {
        RateLimitConfig config = getConfigForAction(action);
        return isAllowed(key, action, config.maxAttempts, config.timeWindow);
    }

    @Override
    public boolean isAllowed(String key, String action, int maxAttempts, Duration timeWindow) {
        try {
            // Vérification de la whitelist
            if (isIpWhitelisted(key)) {
                return true;
            }

            // Vérification de la blacklist
            if (isIpBlacklisted(key)) {
                log.warn("Accès refusé pour clé blacklistée: {}", key);
                return false;
            }

            String cacheKey = key + ":" + action;
            List<LocalDateTime> attempts = attemptCache.getOrDefault(cacheKey, new ArrayList<>());

            // Nettoyage des anciennes tentatives
            LocalDateTime cutoff = LocalDateTime.now().minus(timeWindow);
            attempts.removeIf(attempt -> attempt.isBefore(cutoff));

            // Vérification du nombre de tentatives
            boolean allowed = attempts.size() < maxAttempts;
            
            if (!allowed) {
                log.warn("Rate limit dépassé pour {}: {} tentatives en {}", 
                    cacheKey, attempts.size(), timeWindow);
            }

            return allowed;

        } catch (Exception e) {
            log.error("Erreur vérification rate limit pour {} - {}", key, e.getMessage());
            return true; // En cas d'erreur, on autorise par défaut
        }
    }

    @Override
    public void recordAttempt(String key, String action) {
        try {
            String cacheKey = key + ":" + action;
            List<LocalDateTime> attempts = attemptCache.computeIfAbsent(cacheKey, k -> new ArrayList<>());
            
            attempts.add(LocalDateTime.now());
            
            // Limitation de la taille du cache (garde les 100 dernières tentatives)
            if (attempts.size() > 100) {
                attempts.subList(0, attempts.size() - 100).clear();
            }

            log.debug("Tentative enregistrée pour {}: {} tentatives totales", cacheKey, attempts.size());

        } catch (Exception e) {
            log.error("Erreur enregistrement tentative pour {} - {}", key, e.getMessage());
        }
    }

    @Override
    public int getRemainingAttempts(String key, String action) {
        try {
            RateLimitConfig config = getConfigForAction(action);
            String cacheKey = key + ":" + action;
            List<LocalDateTime> attempts = attemptCache.getOrDefault(cacheKey, new ArrayList<>());

            // Nettoyage des anciennes tentatives
            LocalDateTime cutoff = LocalDateTime.now().minus(config.timeWindow);
            attempts.removeIf(attempt -> attempt.isBefore(cutoff));

            return Math.max(0, config.maxAttempts - attempts.size());

        } catch (Exception e) {
            log.error("Erreur calcul tentatives restantes pour {} - {}", key, e.getMessage());
            return 0;
        }
    }

    @Override
    public long getTimeUntilReset(String key, String action) {
        try {
            RateLimitConfig config = getConfigForAction(action);
            String cacheKey = key + ":" + action;
            List<LocalDateTime> attempts = attemptCache.getOrDefault(cacheKey, new ArrayList<>());

            if (attempts.isEmpty()) {
                return 0;
            }

            // Trouve la plus ancienne tentative dans la fenêtre de temps
            LocalDateTime cutoff = LocalDateTime.now().minus(config.timeWindow);
            LocalDateTime oldestAttempt = attempts.stream()
                .filter(attempt -> attempt.isAfter(cutoff))
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

            LocalDateTime resetTime = oldestAttempt.plus(config.timeWindow);
            return Duration.between(LocalDateTime.now(), resetTime).getSeconds();

        } catch (Exception e) {
            log.error("Erreur calcul temps jusqu'à reset pour {} - {}", key, e.getMessage());
            return 0;
        }
    }

    // ==================== AUTHENTICATION RATE LIMITING ====================

    @Override
    public boolean isLoginAllowed(String ipAddress, String email) {
        return isAllowed(ipAddress, "login") && isAllowed(email, "login");
    }

    @Override
    public void recordFailedLogin(String ipAddress, String email) {
        recordAttempt(ipAddress, "login");
        recordAttempt(email, "login");
        
        // Auto-blacklist après trop de tentatives
        if (getRemainingAttempts(ipAddress, "login") == 0) {
            blacklistIp(ipAddress, "Trop de tentatives de connexion", Duration.ofHours(1));
        }
    }

    @Override
    public void recordSuccessfulLogin(String ipAddress, String email) {
        // Reset des compteurs après connexion réussie
        resetCounters(ipAddress, "login");
        resetCounters(email, "login");
    }

    @Override
    public boolean isRegistrationAllowed(String ipAddress) {
        return isAllowed(ipAddress, "register");
    }

    @Override
    public void recordRegistrationAttempt(String ipAddress) {
        recordAttempt(ipAddress, "register");
    }

    @Override
    public boolean isPasswordResetAllowed(String ipAddress, String email) {
        return isAllowed(ipAddress, "password_reset") && isAllowed(email, "password_reset");
    }

    @Override
    public void recordPasswordResetAttempt(String ipAddress, String email) {
        recordAttempt(ipAddress, "password_reset");
        recordAttempt(email, "password_reset");
    }

    // ==================== EMAIL RATE LIMITING ====================

    @Override
    public boolean isEmailVerificationAllowed(String email) {
        return isAllowed(email, "email_verification");
    }

    @Override
    public void recordEmailVerificationSent(String email) {
        recordAttempt(email, "email_verification");
    }

    @Override
    public boolean isEmailSendingAllowed(String email, String emailType) {
        return isAllowed(email, "email_" + emailType);
    }

    @Override
    public void recordEmailSent(String email, String emailType) {
        recordAttempt(email, "email_" + emailType);
    }

    // ==================== API RATE LIMITING ====================

    @Override
    public boolean isApiAccessAllowed(String apiKey, String endpoint) {
        return isAllowed(apiKey + ":" + endpoint, "api_call");
    }

    @Override
    public void recordApiCall(String apiKey, String endpoint) {
        recordAttempt(apiKey + ":" + endpoint, "api_call");
    }

    @Override
    public boolean isIpAccessAllowed(String ipAddress, String endpoint) {
        return isAllowed(ipAddress + ":" + endpoint, "api_call");
    }

    @Override
    public void recordIpAccess(String ipAddress, String endpoint) {
        recordAttempt(ipAddress + ":" + endpoint, "api_call");
    }

    // ==================== SOCIAL AUTH RATE LIMITING ====================

    @Override
    public boolean isSocialAuthAllowed(String ipAddress, String provider) {
        return isAllowed(ipAddress + ":" + provider, "social_auth");
    }

    @Override
    public void recordSocialAuthAttempt(String ipAddress, String provider) {
        recordAttempt(ipAddress + ":" + provider, "social_auth");
    }

    // ==================== BLACKLIST MANAGEMENT ====================

    @Override
    public void blacklistIp(String ipAddress, String reason, Duration duration) {
        try {
            LocalDateTime expiresAt = LocalDateTime.now().plus(duration);
            blacklistCache.put(ipAddress, expiresAt);
            
            log.warn("IP blacklistée: {} - Raison: {} - Expire: {}", ipAddress, reason, expiresAt);

        } catch (Exception e) {
            log.error("Erreur blacklist IP {} - {}", ipAddress, e.getMessage());
        }
    }

    @Override
    public void removeIpFromBlacklist(String ipAddress) {
        blacklistCache.remove(ipAddress);
        log.info("IP retirée de la blacklist: {}", ipAddress);
    }

    @Override
    public boolean isIpBlacklisted(String ipAddress) {
        try {
            LocalDateTime expiresAt = blacklistCache.get(ipAddress);
            if (expiresAt == null) {
                return false;
            }

            if (expiresAt.isBefore(LocalDateTime.now())) {
                blacklistCache.remove(ipAddress);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Erreur vérification blacklist pour IP {} - {}", ipAddress, e.getMessage());
            return false;
        }
    }

    @Override
    public void blacklistUser(Long userId, String reason, Duration duration) {
        blacklistIp("user:" + userId, reason, duration);
    }

    @Override
    public boolean isUserBlacklisted(Long userId) {
        return isIpBlacklisted("user:" + userId);
    }

    // ==================== WHITELIST MANAGEMENT ====================

    @Override
    public void whitelistIp(String ipAddress, String reason) {
        whitelistCache.add(ipAddress);
        log.info("IP whitelistée: {} - Raison: {}", ipAddress, reason);
    }

    @Override
    public boolean isIpWhitelisted(String ipAddress) {
        return whitelistCache.contains(ipAddress);
    }

    // ==================== STATISTICS AND MONITORING ====================

    @Override
    public Map<String, Object> getRateLimitStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalCacheEntries", attemptCache.size());
            stats.put("blacklistedIps", blacklistCache.size());
            stats.put("whitelistedIps", whitelistCache.size());
            stats.put("configuredActions", configCache.size());
            
            // Statistiques par action
            Map<String, Integer> actionStats = new HashMap<>();
            for (String key : attemptCache.keySet()) {
                String action = key.contains(":") ? key.substring(key.lastIndexOf(":") + 1) : "unknown";
                actionStats.merge(action, 1, Integer::sum);
            }
            stats.put("attemptsByAction", actionStats);
            
            return stats;

        } catch (Exception e) {
            log.error("Erreur récupération statistiques rate limiting - {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Integer> getAttemptsByIp(String ipAddress) {
        Map<String, Integer> attempts = new HashMap<>();
        
        for (Map.Entry<String, List<LocalDateTime>> entry : attemptCache.entrySet()) {
            if (entry.getKey().startsWith(ipAddress + ":")) {
                String action = entry.getKey().substring(entry.getKey().lastIndexOf(":") + 1);
                attempts.put(action, entry.getValue().size());
            }
        }
        
        return attempts;
    }

    @Override
    public List<String> getMostActiveIps(int limit) {
        Map<String, Integer> ipCounts = new HashMap<>();
        
        for (String key : attemptCache.keySet()) {
            if (key.contains(":")) {
                String ip = key.substring(0, key.indexOf(":"));
                ipCounts.merge(ip, 1, Integer::sum);
            }
        }
        
        return ipCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .toList();
    }

    @Override
    public Map<String, Long> getMostLimitedActions(int limit) {
        Map<String, Long> actionCounts = new HashMap<>();
        
        for (String key : attemptCache.keySet()) {
            if (key.contains(":")) {
                String action = key.substring(key.lastIndexOf(":") + 1);
                actionCounts.merge(action, 1L, Long::sum);
            }
        }
        
        return actionCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .collect(LinkedHashMap::new, 
                (map, entry) -> map.put(entry.getKey(), entry.getValue()), 
                LinkedHashMap::putAll);
    }

    // ==================== CONFIGURATION ====================

    @Override
    public void updateRateLimitConfig(String action, int maxAttempts, Duration timeWindow) {
        configCache.put(action, new RateLimitConfig(maxAttempts, timeWindow));
        log.info("Configuration rate limit mise à jour pour {}: {} tentatives en {}", 
            action, maxAttempts, timeWindow);
    }

    @Override
    public Map<String, Object> getRateLimitConfig(String action) {
        RateLimitConfig config = getConfigForAction(action);
        return Map.of(
            "action", action,
            "maxAttempts", config.maxAttempts,
            "timeWindowSeconds", config.timeWindow.getSeconds()
        );
    }

    // ==================== CLEANUP ====================

    @Override
    public int cleanupExpiredEntries() {
        int cleaned = 0;
        
        try {
            // Nettoyage du cache des tentatives
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            
            Iterator<Map.Entry<String, List<LocalDateTime>>> iterator = attemptCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<LocalDateTime>> entry = iterator.next();
                entry.getValue().removeIf(attempt -> attempt.isBefore(cutoff));
                
                if (entry.getValue().isEmpty()) {
                    iterator.remove();
                    cleaned++;
                }
            }
            
            // Nettoyage de la blacklist
            LocalDateTime now = LocalDateTime.now();
            blacklistCache.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
            
            log.info("Nettoyage rate limiting terminé: {} entrées supprimées", cleaned);
            
        } catch (Exception e) {
            log.error("Erreur nettoyage rate limiting - {}", e.getMessage());
        }
        
        return cleaned;
    }

    @Override
    public void resetCounters(String key, String action) {
        String cacheKey = key + ":" + action;
        attemptCache.remove(cacheKey);
        log.debug("Compteurs remis à zéro pour: {}", cacheKey);
    }

    @Override
    public void resetAllCounters(String key) {
        Iterator<String> iterator = attemptCache.keySet().iterator();
        while (iterator.hasNext()) {
            String cacheKey = iterator.next();
            if (cacheKey.startsWith(key + ":")) {
                iterator.remove();
            }
        }
        log.debug("Tous les compteurs remis à zéro pour: {}", key);
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private RateLimitConfig getConfigForAction(String action) {
        return configCache.getOrDefault(action, 
            DEFAULT_CONFIGS.getOrDefault(action, 
                new RateLimitConfig(10, Duration.ofMinutes(1))));
    }

    // Classe interne pour la configuration
    private static class RateLimitConfig {
        final int maxAttempts;
        final Duration timeWindow;

        RateLimitConfig(int maxAttempts, Duration timeWindow) {
            this.maxAttempts = maxAttempts;
            this.timeWindow = timeWindow;
        }
    }
}
