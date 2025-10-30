package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.model.enums.SecurityRiskLevel;
import com.backend.tutor_app.services.IpGeolocationService;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * (Q) PHASE 2 - ÉTAPE 2.4 : Implémentation du service de géolocalisation IP
 * 
 * MISE À JOUR : Intégration MaxMind GeoIP2 pour géolocalisation précise
 * - Géolocalisation IP avec base de données locale (GeoLite2-City)
 * - Détection de pays, ville, coordonnées GPS
 * - Mode fallback si MaxMind non disponible
 * - Cache intégré pour optimiser les performances
 * 
 * @author TutorApp Team
 * @version 2.0 (MaxMind GeoIP2)
 */
@Service
@Slf4j
public class IpGeolocationServiceImpl implements IpGeolocationService {
    
    private final DatabaseReader databaseReader;
    
    @Value("${app.geoip.fallback-enabled:true}")
    private boolean fallbackEnabled;
    
    // (Q) PHASE 2 - Liste des pays à risque élevé (ISO 3166-1 alpha-2)
    private static final Set<String> HIGH_RISK_COUNTRIES = new HashSet<>(Arrays.asList(
        "KP", // Corée du Nord
        "IR", // Iran
        "SY", // Syrie
        "CU", // Cuba
        "SD", // Soudan
        "SS"  // Soudan du Sud
    ));
    
    // (Q) PHASE 2 - Plages IP privées (RFC 1918 + localhost)
    private static final String[] PRIVATE_IP_RANGES = {
        "10.", "192.168.", "172.16.", "172.17.", "172.18.", "172.19.",
        "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
        "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.",
        "127.", "localhost", "0:0:0:0:0:0:0:1", "::1"
    };
    
    /**
     * Constructeur avec injection du DatabaseReader
     * @param databaseReader Bean créé par GeoIpConfig (peut être null si désactivé)
     */
    @Autowired
    public IpGeolocationServiceImpl(@Autowired(required = false) DatabaseReader databaseReader) {
        this.databaseReader = databaseReader;
        
        if (databaseReader == null) {
            log.warn("⚠️ DatabaseReader est NULL - Mode fallback activé");
            log.warn("   La géolocalisation utilisera des heuristiques basiques");
        } else {
            log.info("✅ IpGeolocationServiceImpl initialisé avec MaxMind GeoIP2");
        }
    }
    
    @Override
    public String getCountryFromIp(String ipAddress) {
        log.debug("🌍 Géolocalisation de l'IP: {}", ipAddress);
        
        // Validation de base
        if (ipAddress == null || ipAddress.isBlank()) {
            return "UNKNOWN";
        }
        
        // Détection IP locale/privée
        if (isPrivateIp(ipAddress)) {
            log.debug("🏠 IP privée/locale détectée: {}", ipAddress);
            return "LOCAL";
        }
        
        // Tentative avec MaxMind GeoIP2
        if (databaseReader != null) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ipAddress);
                CityResponse response = databaseReader.city(inetAddress);
                
                Country country = response.getCountry();
                String countryCode = country.getIsoCode();
                String countryName = country.getName();
                
                // Informations supplémentaires (optionnel, pour logs enrichis)
                City city = response.getCity();
                Location location = response.getLocation();
                
                log.debug("✅ IP {} géolocalisée:", ipAddress);
                log.debug("   - Pays: {} ({})", countryName, countryCode);
                if (city.getName() != null) {
                    log.debug("   - Ville: {}", city.getName());
                }
                if (location.getLatitude() != null && location.getLongitude() != null) {
                    log.debug("   - Coordonnées: {}, {}", location.getLatitude(), location.getLongitude());
                }
                
                return countryCode != null ? countryCode : "UNKNOWN";
                
            } catch (AddressNotFoundException e) {
                log.warn("⚠️ IP {} non trouvée dans la base MaxMind", ipAddress);
                return fallbackToHeuristic(ipAddress);
                
            } catch (GeoIp2Exception e) {
                log.error("❌ Erreur GeoIP2 pour {}: {}", ipAddress, e.getMessage());
                return fallbackToHeuristic(ipAddress);
                
            } catch (Exception e) {
                log.error("❌ Erreur inattendue lors de la géolocalisation de {}: {}", ipAddress, e.getMessage());
                return fallbackToHeuristic(ipAddress);
            }
        }
        
        // Si MaxMind non disponible, utiliser le fallback
        return fallbackToHeuristic(ipAddress);
    }
    
    /**
     * Méthode de fallback utilisant des heuristiques simples
     * Utilisée si MaxMind n'est pas disponible ou échoue
     */
    private String fallbackToHeuristic(String ipAddress) {
        if (!fallbackEnabled) {
            log.warn("⚠️ Fallback désactivé, retour UNKNOWN pour: {}", ipAddress);
            return "UNKNOWN";
        }
        
        log.debug("🔄 Utilisation du mode fallback pour: {}", ipAddress);
        
        // Heuristiques basiques (très limitées)
        if (ipAddress.startsWith("2.") || ipAddress.startsWith("5.")) {
            return "FR"; // France (plages Orange/Free)
        } else if (ipAddress.startsWith("8.8.") || ipAddress.startsWith("8.35.")) {
            return "US"; // Google DNS
        }
        
        log.warn("⚠️ Impossible de géolocaliser l'IP: {}", ipAddress);
        return "UNKNOWN";
    }
    
    @Override
    public boolean isVpnOrProxy(String ipAddress) {
        log.debug("🔍 Vérification VPN/Proxy pour: {}", ipAddress);
        
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }
        
        // TODO: Intégrer MaxMind GeoIP2 Anonymous IP Database (payant)
        // Ou utiliser un service externe :
        // - IPQualityScore (https://www.ipqualityscore.com/)
        // - GetIPIntel (https://getipintel.net/)
        // - IP2Proxy (https://www.ip2location.com/proxy-detection)
        
        // Pour l'instant, on retourne false (pas de détection)
        // L'intégration nécessiterait :
        // 1. Télécharger GeoIP2-Anonymous-IP.mmdb (payant ~50€/mois)
        // 2. Créer un second DatabaseReader dans GeoIpConfig
        // 3. Appeler anonymousIpReader.anonymousIp(inetAddress)
        
        log.debug("⚠️ Détection VPN/Proxy non implémentée (retour false par défaut)");
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
        log.debug("📊 Calcul du risque IP: {} → {}", previousIp, currentIp);
        
        // Même IP = Aucun risque
        if (previousIp != null && previousIp.equals(currentIp)) {
            log.debug("✅ Même IP, risque faible");
            return SecurityRiskLevel.LOW;
        }
        
        // Obtenir les pays via MaxMind GeoIP2
        String previousCountry = getCountryFromIp(previousIp);
        String currentCountry = getCountryFromIp(currentIp);
        
        log.debug("   Pays précédent: {} | Pays actuel: {}", previousCountry, currentCountry);
        
        // Vérifier VPN/Proxy
        boolean isVpn = isVpnOrProxy(currentIp);
        if (isVpn) {
            log.warn("⚠️ VPN/Proxy détecté: {} - Risque ÉLEVÉ", currentIp);
            return SecurityRiskLevel.HIGH;
        }
        
        // Vérifier pays à risque
        if (isHighRiskCountry(currentCountry)) {
            log.warn("⚠️ Connexion depuis un pays à risque: {} - Risque ÉLEVÉ", currentCountry);
            return SecurityRiskLevel.HIGH;
        }
        
        // Même pays = Risque faible
        if (previousCountry.equals(currentCountry)) {
            log.debug("✅ Même pays ({}), risque faible", currentCountry);
            return SecurityRiskLevel.LOW;
        }
        
        // Pays différent = Risque moyen
        log.info("⚠️ Changement de pays détecté: {} → {} - Risque MOYEN", previousCountry, currentCountry);
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
