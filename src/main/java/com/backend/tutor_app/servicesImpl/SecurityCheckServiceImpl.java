package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.DeviceInfoDto;
import com.backend.tutor_app.dto.Auth.SecurityCheckResult;
import com.backend.tutor_app.model.enums.DeviceChangeType;
import com.backend.tutor_app.model.enums.SecurityRiskLevel;
import com.backend.tutor_app.model.support.RefreshToken;
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
    
    @Override
    public SecurityCheckResult performSecurityChecks(RefreshToken token, DeviceInfoDto currentDeviceInfo) {
        log.info("(Q) PHASE 2 - Démarrage des vérifications de sécurité pour le token: {}", token.getId());
        
        SecurityCheckResult result = SecurityCheckResult.builder().build();
        
        // (Q) PHASE 2 - ÉTAPE 2.3 : Vérification si token révoqué
        if (token.getIsRevoked()) {
            log.error("(Q) PHASE 2 - ALERTE CRITIQUE : Token révoqué réutilisé ! Token ID: {}", token.getId());
            return SecurityCheckResult.criticalRisk(
                "Token révoqué réutilisé - Possible attaque en cours"
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
        
        log.debug("(Q) PHASE 2 - Vérification IP: {} → {}", previousIp, currentIp);
        
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
            log.warn("(Q) PHASE 2 - VPN/Proxy détecté: {}", currentIp);
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
                log.debug("(Q) PHASE 2 - Risque IP FAIBLE");
                break;
                
            case MEDIUM:
                result.setAllowed(true);
                result.setRequireEmailAlert(true);
                result.setRequireConfirmation(false);
                result.setMessage(String.format("Connexion depuis un nouveau pays: %s", currentCountry));
                result.setSecurityAlert(String.format("Connexion détectée depuis %s", currentCountry));
                log.info("(Q) PHASE 2 - Risque IP MOYEN : {} → {}", previousCountry, currentCountry);
                break;
                
            case HIGH:
                result.setAllowed(false);
                result.setRequireEmailAlert(true);
                result.setRequireSmsAlert(true);
                result.setRequireConfirmation(true);
                result.setShouldBlock(true);
                result.setMessage("Connexion depuis une IP à risque élevé");
                result.setSecurityAlert("Connexion suspecte bloquée. Veuillez confirmer votre identité.");
                log.warn("(Q) PHASE 2 - Risque IP ÉLEVÉ : VPN={}, Pays={}", isVpn, currentCountry);
                break;
        }
        
        return result;
    }
    
    /**
     * (Q) PHASE 2 - ÉTAPE 2.5 : Vérification du Device
     */
    private SecurityCheckResult checkDevice(RefreshToken token, DeviceInfoDto currentDeviceInfo) {
        log.debug("(Q) PHASE 2 - Vérification Device");
        
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
                log.debug("(Q) PHASE 2 - Aucun changement de device");
                break;
                
            case MINOR:
                result.setRiskLevel(SecurityRiskLevel.LOW);
                result.setAllowed(true);
                result.setRequireEmailAlert(false);
                result.setMessage("Mise à jour de navigateur/OS détectée");
                log.info("(Q) PHASE 2 - Changement mineur : {}", 
                    deviceComparisonService.getChangeDescription(previousDevice, currentDeviceInfo));
                break;
                
            case MAJOR:
                result.setRiskLevel(SecurityRiskLevel.MEDIUM);
                result.setAllowed(true);
                result.setRequireEmailAlert(true);
                result.setMessage("Changement de device détecté");
                result.setSecurityAlert("Connexion depuis un nouvel appareil détectée");
                log.warn("(Q) PHASE 2 - Changement majeur : {}", 
                    deviceComparisonService.getChangeDescription(previousDevice, currentDeviceInfo));
                break;
                
            case SUSPICIOUS:
                result.setRiskLevel(SecurityRiskLevel.HIGH);
                result.setAllowed(true); // (Q) Permettre mais alerter fortement
                result.setRequireEmailAlert(true);
                result.setRequireSmsAlert(false);
                result.setRequireConfirmation(false);
                result.setMessage("Changement de device suspect");
                result.setSecurityAlert(" Connexion depuis un appareil très différent");
                log.warn("(Q) PHASE 2 - Changement SUSPECT : {}", 
                    deviceComparisonService.getChangeDescription(previousDevice, currentDeviceInfo));
                break;
        }
        
        return result;
    }
    
    /**
     * (Q) PHASE 2 - Combine les résultats IP et Device pour déterminer le risque global
     */
    private SecurityCheckResult combineResults(SecurityCheckResult ipCheck, SecurityCheckResult deviceCheck) {
        log.debug("(Q) PHASE 2 - Combinaison des résultats : IP={}, Device={}", 
            ipCheck.getRiskLevel(), 
            deviceCheck.getRiskLevel());
        
        // (Q) PHASE 2 - Le risque le plus élevé l'emporte
        SecurityRiskLevel globalRisk = getHighestRisk(ipCheck.getRiskLevel(), deviceCheck.getRiskLevel());
        
        SecurityCheckResult combined = SecurityCheckResult.builder()
            .riskLevel(globalRisk)
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
        
        log.info("(Q) PHASE 2 - Résultat global : Risque={}, Autorisé={}, Alertes=Email:{}/SMS:{}", 
            combined.getRiskLevel(), 
            combined.isAllowed(),
            combined.isRequireEmailAlert(),
            combined.isRequireSmsAlert());
        
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
