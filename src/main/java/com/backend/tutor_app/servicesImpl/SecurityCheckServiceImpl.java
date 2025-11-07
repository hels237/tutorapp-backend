package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.DeviceInfoDto;
import com.backend.tutor_app.dto.Auth.SecurityCheckResult;
import com.backend.tutor_app.model.enums.DeviceChangeType;
import com.backend.tutor_app.model.enums.SecurityRiskLevel;
import com.backend.tutor_app.model.support.RefreshToken;
import com.backend.tutor_app.services.AttackPatternDetectionService;
import com.backend.tutor_app.services.DeviceComparisonService;
import com.backend.tutor_app.services.IpGeolocationService;
import com.backend.tutor_app.services.SecurityCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * (Q) PHASE 2 - Implémentation du service de vérification de sécurité
 * Orchestre toutes les vérifications selon le flow de la PHASE 2
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityCheckServiceImpl implements SecurityCheckService {
    
    private final IpGeolocationService ipGeolocationService;
    private final DeviceComparisonService deviceComparisonService;
    private final AttackPatternDetectionService attackPatternDetectionService;
    
    @Override
    public SecurityCheckResult performSecurityChecks(RefreshToken token, DeviceInfoDto currentDeviceInfo) {
        log.debug("[PHASE 4] Démarrage vérifications sécurité - TokenID: {}, IP: {}, Device: {}", 
            token.getId(), currentDeviceInfo.getIpAddress(), currentDeviceInfo.getDeviceSummary());
        
        SecurityCheckResult result = SecurityCheckResult.builder().build();
        
        // (Q) PHASE 2 - ÉTAPE 2.3 : Vérification si token révoqué
        if (token.getIsRevoked()) {
            log.error("[PHASE 4][CRITICAL] Token révoqué réutilisé - TokenID: {}, UserID: {}, IP: {}, Attaque potentielle détectée", 
                token.getId(), token.getUtilisateur().getId(), currentDeviceInfo.getIpAddress());
            
            // (PHASE 4) Enregistrer tentative suspecte
            attackPatternDetectionService.recordSuspiciousAttempt(
                token.getUtilisateur().getId(), 
                "Token révoqué réutilisé"
            );
            
            return SecurityCheckResult.criticalRisk(
                "Token révoqué réutilisé - Possible attaque en cours"
            );
        }
        
        // (PHASE 4) Vérifier pattern d'attaque AVANT les autres vérifications
        if (attackPatternDetectionService.hasAttackPattern(token.getUtilisateur().getId())) {
            log.error("[PHASE 4][CRITICAL] Pattern d'attaque détecté - UserID: {}, Tentatives: {}", 
                token.getUtilisateur().getId(),
                attackPatternDetectionService.getRecentSuspiciousAttempts(token.getUtilisateur().getId()));
            
            return SecurityCheckResult.criticalRisk(
                "Pattern d'attaque détecté - Multiples tentatives suspectes"
            );
        }
        
        // (Q) PHASE 2 - ÉTAPE 2.4 : Vérification IP
        SecurityCheckResult ipCheck = checkIpAddress(token, currentDeviceInfo);
        
        // (Q) PHASE 2 - ÉTAPE 2.5 : Vérification Device
        SecurityCheckResult deviceCheck = checkDevice(token, currentDeviceInfo);
        
        // (Q) PHASE 2 - Combiner les résultats et déterminer le risque global
        return combineResults(ipCheck, deviceCheck);
    }
    
    /**
     * (Q) PHASE 2 - ÉTAPE 2.4 : Vérification de l'adresse IP
     */
    private SecurityCheckResult checkIpAddress(RefreshToken token, DeviceInfoDto currentDeviceInfo) {
        String previousIp = token.getIpAddress();
        String currentIp = currentDeviceInfo.getIpAddress();
        
        log.debug("[PHASE 4] Vérification IP - Previous: {}, Current: {}", previousIp, currentIp);
        
        SecurityCheckResult result = SecurityCheckResult.builder()
            .previousIp(previousIp)
            .currentIp(currentIp)
            .build();
        
        // (Q) PHASE 2 - Même IP = Aucun risque
        if (previousIp != null && previousIp.equals(currentIp)) {
            result.setIpChanged(false);
            result.setRiskLevel(SecurityRiskLevel.LOW);
            result.setAllowed(true);
            return result;
        }
        
        result.setIpChanged(true);
        
        // (Q) PHASE 2 - Obtenir les pays
        String previousCountry = ipGeolocationService.getCountryFromIp(previousIp);
        String currentCountry = ipGeolocationService.getCountryFromIp(currentIp);
        
        result.setPreviousCountry(previousCountry);
        result.setCurrentCountry(currentCountry);
        
        // (Q) PHASE 2 - Vérifier VPN/Proxy
        boolean isVpn = ipGeolocationService.isVpnOrProxy(currentIp);
        result.setVpnDetected(isVpn);
        
        if (isVpn) {
            log.error("[PHASE 4][ERROR] VPN/Proxy détecté - IP: {}, Pays: {}", currentIp, currentCountry);
        }
        
        // (Q) PHASE 2 - Calculer le niveau de risque
        SecurityRiskLevel ipRiskLevel = ipGeolocationService.calculateIpRiskLevel(previousIp, currentIp);
        result.setRiskLevel(ipRiskLevel);
        
        // (Q) PHASE 2 - Déterminer les actions selon le risque
        switch (ipRiskLevel) {
            case LOW:
                result.setAllowed(true);
                result.setRequireEmailAlert(false);
                result.setMessage("Changement d'IP dans le même pays");
                log.info("[PHASE 4][INFO] Risque IP faible - IP changée même pays: {} → {}", previousCountry, currentCountry);
                break;
                
            case MEDIUM:
                result.setAllowed(true);
                result.setRequireEmailAlert(true);
                result.setRequireConfirmation(false);
                result.setMessage(String.format("Connexion depuis un nouveau pays: %s", currentCountry));
                result.setSecurityAlert(String.format("Connexion détectée depuis %s", currentCountry));
                log.warn("[PHASE 4][WARNING] Risque IP moyen - Changement pays: {} → {}, IP: {}", 
                    previousCountry, currentCountry, currentIp);
                break;
                
            case HIGH:
                result.setAllowed(false);
                result.setRequireEmailAlert(true);
                result.setRequireSmsAlert(true);
                result.setRequireConfirmation(true);
                result.setShouldBlock(true);
                result.setMessage("Connexion depuis une IP à risque élevé");
                result.setSecurityAlert("Connexion suspecte bloquée. Veuillez confirmer votre identité.");
                log.error("[PHASE 4][ERROR] Risque IP élevé - VPN: {}, Pays: {}, IP: {}", 
                    isVpn, currentCountry, currentIp);
                
                // (PHASE 4) Enregistrer tentative suspecte
                attackPatternDetectionService.recordSuspiciousAttempt(
                    token.getUtilisateur().getId(),
                    String.format("IP à risque élevé: VPN=%s, Pays=%s", isVpn, currentCountry)
                );
                break;
        }
        
        return result;
    }
    
    /**
     * (Q) PHASE 2 - ÉTAPE 2.5 : Vérification du Device
     */
    private SecurityCheckResult checkDevice(RefreshToken token, DeviceInfoDto currentDeviceInfo) {
        // (Q) PHASE 2 - Construire le DeviceInfoDto précédent depuis le token
        DeviceInfoDto previousDevice = DeviceInfoDto.builder()
            .browserName(token.getBrowserName())
            .browserVersion(token.getBrowserVersion())
            .osName(token.getOsName())
            .osVersion(token.getOsVersion())
            .timezone(token.getTimezone())
            .browserLanguage(token.getBrowserLanguage())
            .userAgent(token.getUserAgent())
            .ipAddress(token.getIpAddress())
            .build();
        
        log.debug("[PHASE 4] Vérification Device - Previous: {}, Current: {}", 
            previousDevice.getDeviceSummary(), currentDeviceInfo.getDeviceSummary());
        
        SecurityCheckResult result = SecurityCheckResult.builder()
            .previousDevice(previousDevice.getDeviceSummary())
            .currentDevice(currentDeviceInfo.getDeviceSummary())
            .previousBrowser(token.getBrowserName())
            .currentBrowser(currentDeviceInfo.getBrowserName())
            .previousOs(token.getOsName())
            .currentOs(currentDeviceInfo.getOsName())
            .build();
        
        // (Q) PHASE 2 - Comparer les devices
        DeviceChangeType changeType = deviceComparisonService.compareDevices(previousDevice, currentDeviceInfo);
        result.setDeviceChangeType(changeType);
        
        // (Q) PHASE 2 - Déterminer les actions selon le type de changement
        switch (changeType) {
            case NONE:
                result.setRiskLevel(SecurityRiskLevel.LOW);
                result.setAllowed(true);
                result.setRequireEmailAlert(false);
                result.setMessage("Aucun changement de device");
                log.info("[PHASE 4][INFO] Aucun changement device - Device: {}", currentDeviceInfo.getDeviceSummary());
                break;
                
            case MINOR:
                result.setRiskLevel(SecurityRiskLevel.LOW);
                result.setAllowed(true);
                result.setRequireEmailAlert(false);
                result.setMessage("Mise à jour de navigateur/OS détectée");
                log.info("[PHASE 4][INFO] Changement device mineur - Details: {}", 
                    deviceComparisonService.getChangeDescription(previousDevice, currentDeviceInfo));
                break;
                
            case MAJOR:
                result.setRiskLevel(SecurityRiskLevel.MEDIUM);
                result.setAllowed(true);
                result.setRequireEmailAlert(true);
                result.setMessage("Changement de device détecté");
                result.setSecurityAlert("Connexion depuis un nouvel appareil détectée");
                log.warn("[PHASE 4][WARNING] Changement device majeur - Details: {}, Previous: {}, Current: {}", 
                    deviceComparisonService.getChangeDescription(previousDevice, currentDeviceInfo),
                    previousDevice.getDeviceSummary(), currentDeviceInfo.getDeviceSummary());
                break;
                
            case SUSPICIOUS:
                result.setRiskLevel(SecurityRiskLevel.HIGH);
                result.setAllowed(true); // (Q) Permettre mais alerter fortement
                result.setRequireEmailAlert(true);
                result.setRequireSmsAlert(false);
                result.setRequireConfirmation(false);
                result.setMessage("Changement de device suspect");
                result.setSecurityAlert(" Connexion depuis un appareil très différent");
                log.error("[PHASE 4][ERROR] Changement device suspect - Details: {}, Previous: {}, Current: {}", 
                    deviceComparisonService.getChangeDescription(previousDevice, currentDeviceInfo),
                    previousDevice.getDeviceSummary(), currentDeviceInfo.getDeviceSummary());
                break;
        }
        
        return result;
    }
    
    /**
     * (Q) PHASE 2 - Combine les résultats IP et Device pour déterminer le risque global
     */
    private SecurityCheckResult combineResults(SecurityCheckResult ipCheck, SecurityCheckResult deviceCheck) {
        log.debug("[PHASE 4] Combinaison résultats - RisqueIP: {}, RisqueDevice: {}", 
            ipCheck.getRiskLevel(), deviceCheck.getRiskLevel());
        
        // (Q) PHASE 2 - Le risque le plus élevé l'emporte
        SecurityRiskLevel globalRisk = getHighestRisk(ipCheck.getRiskLevel(), deviceCheck.getRiskLevel());
        
        SecurityCheckResult combined = SecurityCheckResult.builder()
            .riskLevel(globalRisk)
            .alertLevel(com.backend.tutor_app.model.enums.AlertLevel.fromSecurityRiskLevel(globalRisk))
            .allowed(ipCheck.isAllowed() && deviceCheck.isAllowed())
            .message(buildCombinedMessage(ipCheck, deviceCheck))
            // (Q) Données IP
            .ipChanged(ipCheck.isIpChanged())
            .previousIp(ipCheck.getPreviousIp())
            .currentIp(ipCheck.getCurrentIp())
            .previousCountry(ipCheck.getPreviousCountry())
            .currentCountry(ipCheck.getCurrentCountry())
            .vpnDetected(ipCheck.isVpnDetected())
            // (Q) Données Device
            .deviceChangeType(deviceCheck.getDeviceChangeType())
            .previousDevice(deviceCheck.getPreviousDevice())
            .currentDevice(deviceCheck.getCurrentDevice())
            .previousBrowser(deviceCheck.getPreviousBrowser())
            .currentBrowser(deviceCheck.getCurrentBrowser())
            .previousOs(deviceCheck.getPreviousOs())
            .currentOs(deviceCheck.getCurrentOs())
            // (Q) Actions requises (le plus restrictif)
            .requireEmailAlert(ipCheck.isRequireEmailAlert() || deviceCheck.isRequireEmailAlert())
            .requireSmsAlert(ipCheck.isRequireSmsAlert() || deviceCheck.isRequireSmsAlert())
            .requireConfirmation(ipCheck.isRequireConfirmation() || deviceCheck.isRequireConfirmation())
            .requireAdminNotification(ipCheck.isRequireAdminNotification() || deviceCheck.isRequireAdminNotification())
            .shouldBlock(ipCheck.isShouldBlock() || deviceCheck.isShouldBlock())
            // (Q) Alerte sécurité
            .securityAlert(buildSecurityAlert(ipCheck, deviceCheck))
            .build();
        
        String logMessage = String.format(
            "[PHASE 4] Résultat global - AlertLevel: %s, RiskLevel: %s, Autorisé: %s, Email: %s, SMS: %s, Blocage: %s",
            combined.getAlertLevel(), combined.getRiskLevel(), combined.isAllowed(),
            combined.isRequireEmailAlert(), combined.isRequireSmsAlert(), combined.isShouldBlock()
        );
        
        switch (combined.getAlertLevel()) {
            case INFO:
                log.info(logMessage);
                break;
            case WARNING:
                log.warn(logMessage);
                break;
            case ERROR:
                log.error(logMessage);
                break;
            case CRITICAL:
                log.error("[CRITICAL] " + logMessage);
                break;
        }
        
        return combined;
    }
    
    /**
     * (Q) PHASE 2 - Détermine le risque le plus élevé
     */
    private SecurityRiskLevel getHighestRisk(SecurityRiskLevel risk1, SecurityRiskLevel risk2) {
        if (risk1 == SecurityRiskLevel.CRITICAL || risk2 == SecurityRiskLevel.CRITICAL) {
            return SecurityRiskLevel.CRITICAL;
        }
        if (risk1 == SecurityRiskLevel.HIGH || risk2 == SecurityRiskLevel.HIGH) {
            return SecurityRiskLevel.HIGH;
        }
        if (risk1 == SecurityRiskLevel.MEDIUM || risk2 == SecurityRiskLevel.MEDIUM) {
            return SecurityRiskLevel.MEDIUM;
        }
        return SecurityRiskLevel.LOW;
    }
    
    /**
     * (Q) PHASE 2 - Construit un message combiné
     */
    private String buildCombinedMessage(SecurityCheckResult ipCheck, SecurityCheckResult deviceCheck) {
        if (ipCheck.getRiskLevel() == SecurityRiskLevel.LOW && 
            deviceCheck.getRiskLevel() == SecurityRiskLevel.LOW) {
            return "Aucun risque détecté";
        }
        
        StringBuilder message = new StringBuilder();
        
        if (ipCheck.getRiskLevel() != SecurityRiskLevel.LOW) {
            message.append(ipCheck.getMessage());
        }
        
        if (deviceCheck.getRiskLevel() != SecurityRiskLevel.LOW) {
            if (message.length() > 0) {
                message.append(" + ");
            }
            message.append(deviceCheck.getMessage());
        }
        
        return message.toString();
    }
    
    /**
     * (Q) PHASE 2 - Construit l'alerte sécurité pour l'utilisateur
     */
    private String buildSecurityAlert(SecurityCheckResult ipCheck, SecurityCheckResult deviceCheck) {
        // (Q) Prioriser l'alerte la plus importante
        if (ipCheck.getSecurityAlert() != null && 
            ipCheck.getRiskLevel().ordinal() >= deviceCheck.getRiskLevel().ordinal()) {
            return ipCheck.getSecurityAlert();
        }
        
        if (deviceCheck.getSecurityAlert() != null) {
            return deviceCheck.getSecurityAlert();
        }
        
        return null;
    }
}
