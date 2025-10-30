package com.backend.tutor_app.config;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration MaxMind GeoIP2
 * Initialise le DatabaseReader pour la géolocalisation IP précise
 * 
 * @author TutorApp Team
 * @version 1.0
 */
@Configuration
@Slf4j
public class GeoIpConfig {
    
    @Value("${app.geoip.database-path}")
    private Resource databaseFile;
    
    @Value("${app.geoip.enabled:true}")
    private boolean geoipEnabled;
    
    @Value("${app.geoip.cache-size:4096}")
    private int cacheSize;
    
    @Value("${app.geoip.account-id:}")
    private String accountId;
    
    @Value("${app.geoip.license-key:}")
    private String licenseKey;
    
    /**
     * Crée le Bean DatabaseReader pour MaxMind GeoIP2
     * Ce bean sera injecté dans IpGeolocationServiceImpl
     * 
     * @return DatabaseReader configuré avec cache, ou null si désactivé
     */
    @Bean
    public DatabaseReader databaseReader() {
        if (!geoipEnabled) {
            log.warn("⚠️ MaxMind GeoIP2 est DÉSACTIVÉ dans la configuration");
            log.warn("   La géolocalisation IP utilisera le mode fallback (heuristiques)");
            return null;
        }
        
        log.info("🌍 Initialisation de MaxMind GeoIP2 DatabaseReader...");
        log.info("   - Account ID: {}", accountId != null && !accountId.isEmpty() ? accountId : "Non configuré");
        log.info("   - Database: {}", databaseFile.getFilename());
        log.info("   - Cache size: {} entrées (~{} MB RAM)", cacheSize, (cacheSize * 4) / 1024);
        
        try (InputStream inputStream = databaseFile.getInputStream()) {
            // Création du DatabaseReader avec cache CHM (ConcurrentHashMap)
            // pour optimiser les performances des requêtes répétées
            DatabaseReader reader = new DatabaseReader.Builder(inputStream)
                .withCache(new CHMCache(cacheSize))
                .build();
            
            log.info("✅ MaxMind GeoIP2 DatabaseReader initialisé avec succès");
            log.info("   Type de base: {}", reader.getMetadata().getDatabaseType());
            log.info("   Date de build: {}", reader.getMetadata().getBuildDate());
            
            return reader;
            
        } catch (IOException e) {
            log.error("❌ Erreur lors de l'initialisation de MaxMind GeoIP2");
            log.error("   Message: {}", e.getMessage());
            log.error("   Fichier: {}", databaseFile.getFilename());
            log.error("   Vérifiez que le fichier GeoLite2-City.mmdb existe dans src/main/resources/geoip/");
            log.error("   Téléchargement: https://www.maxmind.com/en/accounts/current/geoip/downloads");
            
            // En développement, on retourne null pour permettre le démarrage
            // En production, vous pourriez vouloir throw l'exception
            log.warn("⚠️ L'application démarrera en mode fallback (sans MaxMind)");
            return null;
        }
    }
    
    /**
     * Méthode appelée lors de la destruction du bean
     * Ferme proprement le DatabaseReader pour libérer les ressources
     */
    @Bean(destroyMethod = "close")
    public DatabaseReader databaseReaderWithCleanup() {
        DatabaseReader reader = databaseReader();
        
        if (reader != null) {
            // Enregistrer un hook de fermeture
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("🔒 Fermeture de MaxMind GeoIP2 DatabaseReader...");
                    reader.close();
                    log.info("✅ MaxMind GeoIP2 DatabaseReader fermé proprement");
                } catch (IOException e) {
                    log.error("❌ Erreur lors de la fermeture de DatabaseReader: {}", e.getMessage());
                }
            }));
        }
        
        return reader;
    }
}
