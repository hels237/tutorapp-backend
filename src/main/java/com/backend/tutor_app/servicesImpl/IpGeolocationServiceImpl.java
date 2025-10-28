package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.model.enums.SecurityRiskLevel;
import com.backend.tutor_app.services.IpGeolocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * (Q) PHASE 2 - ÉTAPE 2.4 : Implémentation du service de géolocalisation IP
 * 
 * NOTE : Cette implémentation est basique et utilise des heuristiques simples.
 * Pour la production, il est recommandé d'utiliser un service externe comme :
 * - MaxMind GeoIP2
 * - IP2Location
 * - ipapi.co
 * - ipgeolocation.io
 */
@Service
@Slf4j
public class IpGeolocationServiceImpl implements IpGeolocationService {
    
    // (Q) PHASE 2 - Liste des pays à risque élevé (exemple simplifié)
    private static final Set<String> HIGH_RISK_COUNTRIES = new HashSet<>(Arrays.asList(
        "KP", // Corée du Nord
        "IR", // Iran
        "SY", // Syrie
        "CU"  // Cuba
        // Ajouter d'autres pays selon les besoins
    ));
    
    // (Q) PHASE 2 - Plages IP privées (pour détection locale)
    private static final String[] PRIVATE_IP_RANGES = {
        "10.", "192.168.", "172.16.", "172.17.", "172.18.", "172.19.",
        "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
        "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.",
        "127.", "localhost"
    };
    
    @Override
    public String getCountryFromIp(String ipAddress) {
        log.debug("(Q) PHASE 2 - Géolocalisation de l'IP: {}", ipAddress);
        
        if (ipAddress == null || ipAddress.isBlank()) {
            return "UNKNOWN";
        }
        
        // (Q) PHASE 2 - Détection IP locale/privée
        if (isPrivateIp(ipAddress)) {
            log.debug("(Q) PHASE 2 - IP privée détectée: {}", ipAddress);
            return "LOCAL";
        }
        
        // (Q) PHASE 2 - TODO: Intégrer un vrai service de géolocalisation
        // Pour l'instant, on retourne un pays par défaut basé sur des heuristiques simples
        
        // Exemple d'heuristique simple (à remplacer par un vrai service)
        if (ipAddress.startsWith("2.") || ipAddress.startsWith("5.")) {
            return "FR"; // France (exemple)
        }
        
        log.warn("(Q) PHASE 2 - Impossible de géolocaliser l'IP: {}", ipAddress);
        return "UNKNOWN";
    }
    
    @Override
    public boolean isVpnOrProxy(String ipAddress) {
        log.debug("(Q) PHASE 2 - Vérification VPN/Proxy pour: {}", ipAddress);
        
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }
        
        // (Q) PHASE 2 - TODO: Intégrer un service de détection VPN/Proxy
        // Services recommandés :
        // - IPQualityScore
        // - GetIPIntel
        // - IP2Proxy
        
        // Pour l'instant, on retourne false (pas de détection)
        // Dans une vraie implémentation, on ferait un appel API ici
        
        return false;
    }
    
    @Override
    public boolean isHighRiskCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return false;
        }
        
        boolean isHighRisk = HIGH_RISK_COUNTRIES.contains(countryCode.toUpperCase());
        
        if (isHighRisk) {
            log.warn("(Q) PHASE 2 - Pays à risque élevé détecté: {}", countryCode);
        }
        
        return isHighRisk;
    }
    
    @Override
    public SecurityRiskLevel calculateIpRiskLevel(String previousIp, String currentIp) {
        log.debug("(Q) PHASE 2 - Calcul du risque IP: {} → {}", previousIp, currentIp);
        
        // (Q) PHASE 2 - Même IP = Aucun risque
        if (previousIp != null && previousIp.equals(currentIp)) {
            return SecurityRiskLevel.LOW;
        }
        
        // (Q) PHASE 2 - Obtenir les pays
        String previousCountry = getCountryFromIp(previousIp);
        String currentCountry = getCountryFromIp(currentIp);
        
        // (Q) PHASE 2 - Vérifier VPN/Proxy
        boolean isVpn = isVpnOrProxy(currentIp);
        if (isVpn) {
            log.warn("(Q) PHASE 2 - VPN/Proxy détecté: {}", currentIp);
            return SecurityRiskLevel.HIGH;
        }
        
        // (Q) PHASE 2 - Vérifier pays à risque
        if (isHighRiskCountry(currentCountry)) {
            log.warn("(Q) PHASE 2 - Connexion depuis un pays à risque: {}", currentCountry);
            return SecurityRiskLevel.HIGH;
        }
        
        // (Q) PHASE 2 - Même pays = Risque faible
        if (previousCountry.equals(currentCountry)) {
            log.debug("(Q) PHASE 2 - Même pays, risque faible: {}", currentCountry);
            return SecurityRiskLevel.LOW;
        }
        
        // (Q) PHASE 2 - Pays différent = Risque moyen
        log.info("(Q) PHASE 2 - Changement de pays détecté: {} → {}", previousCountry, currentCountry);
        return SecurityRiskLevel.MEDIUM;
    }
    
    /**
     * (Q) PHASE 2 - Vérifie si une IP est privée/locale
     */
    private boolean isPrivateIp(String ipAddress) {
        for (String prefix : PRIVATE_IP_RANGES) {
            if (ipAddress.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
