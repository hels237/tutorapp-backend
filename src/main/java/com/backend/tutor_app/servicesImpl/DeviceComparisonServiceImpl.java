package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.DeviceInfoDto;
import com.backend.tutor_app.model.enums.DeviceChangeType;
import com.backend.tutor_app.services.DeviceComparisonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * (Q) PHASE 2 - ÉTAPE 2.5 : Implémentation du service de comparaison de devices
 */
@Service
@Slf4j
public class DeviceComparisonServiceImpl implements DeviceComparisonService {
    
    @Override
    public DeviceChangeType compareDevices(DeviceInfoDto previousDevice, DeviceInfoDto currentDevice) {
        log.debug("(Q) PHASE 2 - Comparaison devices: {} vs {}", 
            previousDevice != null ? previousDevice.getDeviceSummary() : "null",
            currentDevice != null ? currentDevice.getDeviceSummary() : "null");
        
        // (Q) PHASE 2 - Si pas de device précédent, considérer comme nouveau
        if (previousDevice == null || currentDevice == null) {
            return DeviceChangeType.MAJOR;
        }
        
        // (Q) PHASE 2 - Comparaison des navigateurs
        String prevBrowser = previousDevice.getBrowserName();
        String currBrowser = currentDevice.getBrowserName();
        String prevBrowserVersion = previousDevice.getBrowserVersion();
        String currBrowserVersion = currentDevice.getBrowserVersion();
        
        // (Q) PHASE 2 - Comparaison des OS
        String prevOs = previousDevice.getOsName();
        String currOs = currentDevice.getOsName();
        String prevOsVersion = previousDevice.getOsVersion();
        String currOsVersion = currentDevice.getOsVersion();
        
        // (PHASE 4) - Comparaison timezone
        String prevTimezone = previousDevice.getTimezone();
        String currTimezone = currentDevice.getTimezone();
        
        // (Q) PHASE 2 - Aucun changement : tout identique
        if (isSame(prevBrowser, currBrowser) && 
            isSame(prevBrowserVersion, currBrowserVersion) &&
            isSame(prevOs, currOs) && 
            isSame(prevOsVersion, currOsVersion)) {
            
            log.debug("(Q) PHASE 2 - Aucun changement de device détecté");
            return DeviceChangeType.NONE;
        }
        
        // (PHASE 4) - Changement timezone seul = INFO (changement mineur)
        if (isSame(prevBrowser, currBrowser) && 
            isSame(prevBrowserVersion, currBrowserVersion) &&
            isSame(prevOs, currOs) && 
            isSame(prevOsVersion, currOsVersion) &&
            !isSame(prevTimezone, currTimezone)) {
            
            log.info("[PHASE 4][INFO] Changement timezone détecté: {} → {}", prevTimezone, currTimezone);
            return DeviceChangeType.MINOR;
        }
        
        // (Q) PHASE 2 - Changement mineur : même browser/OS, versions différentes
        if (isSame(prevBrowser, currBrowser) && isSame(prevOs, currOs)) {
            // Vérifier si c'est juste une mise à jour de version
            if (isVersionUpdate(prevBrowserVersion, currBrowserVersion) ||
                isVersionUpdate(prevOsVersion, currOsVersion)) {
                
                log.info("(Q) PHASE 2 - Changement mineur détecté (mise à jour): {} → {}", 
                    previousDevice.getDeviceSummary(), 
                    currentDevice.getDeviceSummary());
                return DeviceChangeType.MINOR;
            }
        }
        
        // (Q) PHASE 2 - Changement majeur : browser OU OS différent
        if (!isSame(prevBrowser, currBrowser) || !isSame(prevOs, currOs)) {
            log.warn("(Q) PHASE 2 - Changement majeur détecté: {} → {}", 
                previousDevice.getDeviceSummary(), 
                currentDevice.getDeviceSummary());
            
            // (Q) PHASE 2 - Changement suspect : browser ET OS différents
            if (!isSame(prevBrowser, currBrowser) && !isSame(prevOs, currOs)) {
                log.warn("(Q) PHASE 2 - Changement SUSPECT : browser ET OS différents");
                return DeviceChangeType.SUSPICIOUS;
            }
            
            return DeviceChangeType.MAJOR;
        }
        
        // (Q) PHASE 2 - Par défaut, considérer comme majeur
        return DeviceChangeType.MAJOR;
    }
    
    @Override
    public boolean isSuspiciousChange(DeviceChangeType changeType) {
        return changeType == DeviceChangeType.SUSPICIOUS || 
               changeType == DeviceChangeType.MAJOR;
    }
    
    @Override
    public String getChangeDescription(DeviceInfoDto previousDevice, DeviceInfoDto currentDevice) {
        if (previousDevice == null || currentDevice == null) {
            return "Nouveau device détecté";
        }
        
        DeviceChangeType changeType = compareDevices(previousDevice, currentDevice);
        
        switch (changeType) {
            case NONE:
                return "Aucun changement de device";
                
            case MINOR:
                return String.format("Mise à jour détectée: %s → %s", 
                    previousDevice.getDeviceSummary(), 
                    currentDevice.getDeviceSummary());
                
            case MAJOR:
                return String.format("Changement de device: %s → %s", 
                    previousDevice.getDeviceSummary(), 
                    currentDevice.getDeviceSummary());
                
            case SUSPICIOUS:
                return String.format("⚠️ Changement suspect: %s → %s", 
                    previousDevice.getDeviceSummary(), 
                    currentDevice.getDeviceSummary());
                
            default:
                return "Changement de device détecté";
        }
    }
    
    /**
     * (Q) PHASE 2 - Compare deux chaînes de manière sûre
     */
    private boolean isSame(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equalsIgnoreCase(str2);
    }
    
    /**
     * (Q) PHASE 2 - Vérifie si c'est une mise à jour de version (heuristique simple)
     * Ex: "120.0" → "121.0" = true
     * Ex: "120.0" → "119.0" = false (downgrade suspect)
     */
    private boolean isVersionUpdate(String prevVersion, String currVersion) {
        if (prevVersion == null || currVersion == null) {
            return false;
        }
        
        try {
            // Extraire le numéro de version majeure
            String prevMajor = prevVersion.split("\\.")[0];
            String currMajor = currVersion.split("\\.")[0];
            
            int prevNum = Integer.parseInt(prevMajor);
            int currNum = Integer.parseInt(currMajor);
            
            // Considérer comme mise à jour si version actuelle >= précédente
            // et différence <= 5 versions (pour éviter les sauts suspects)
            return currNum >= prevNum && (currNum - prevNum) <= 5;
            
        } catch (Exception e) {
            // En cas d'erreur de parsing, considérer comme changement majeur
            return false;
        }
    }
}
