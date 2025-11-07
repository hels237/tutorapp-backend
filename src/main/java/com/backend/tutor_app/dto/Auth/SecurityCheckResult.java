package com.backend.tutor_app.dto.Auth;

import com.backend.tutor_app.model.enums.AlertLevel;
import com.backend.tutor_app.model.enums.DeviceChangeType;
import com.backend.tutor_app.model.enums.SecurityRiskLevel;
import lombok.*;

/**
 * (Q) PHASE 2 - ÉTAPE 2.4/2.5 : Résultat des vérifications de sécurité
 * Contient toutes les informations sur les vérifications IP et Device
 */
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityCheckResult {
    
    // (Q) PHASE 2 - Résultat global
    private SecurityRiskLevel riskLevel;
    private AlertLevel alertLevel;
    private boolean allowed;
    private String message;
    
    // (Q) PHASE 2 - Vérification IP
    private boolean ipChanged;
    private String previousIp;
    private String currentIp;
    private String previousCountry;
    private String currentCountry;
    private boolean vpnDetected;
    private boolean proxyDetected;
    
    // (Q) PHASE 2 - Vérification Device
    private DeviceChangeType deviceChangeType;
    private String previousDevice;
    private String currentDevice;
    private String previousBrowser;
    private String currentBrowser;
    private String previousOs;
    private String currentOs;
    
    // (Q) PHASE 2 - Actions recommandées
    private boolean requireEmailAlert;
    private boolean requireSmsAlert;
    private boolean requireConfirmation;
    private boolean requireAdminNotification;
    private boolean shouldBlock;
    
    // (Q) PHASE 2 - Message d'alerte pour l'utilisateur (optionnel)
    private String securityAlert;
    
    /**
     * (Q) PHASE 2 - Constructeur pour risque faible (pas de changement)
     */
    public static SecurityCheckResult allowedNoRisk() {
        return SecurityCheckResult.builder()
            .riskLevel(SecurityRiskLevel.LOW)
            .allowed(true)
            .message("No security risk detected")
            .ipChanged(false)
            .deviceChangeType(DeviceChangeType.NONE)
            .requireEmailAlert(false)
            .requireSmsAlert(false)
            .requireConfirmation(false)
            .requireAdminNotification(false)
            .shouldBlock(false)
            .build();
    }
    
    /**
     * (Q) PHASE 2 - Constructeur pour risque critique (attaque détectée)
     */
    public static SecurityCheckResult criticalRisk(String message) {
        return SecurityCheckResult.builder()
            .riskLevel(SecurityRiskLevel.CRITICAL)
            .allowed(false)
            .message(message)
            .requireEmailAlert(true)
            .requireSmsAlert(true)
            .requireConfirmation(true)
            .requireAdminNotification(true)
            .shouldBlock(true)
            .securityAlert("Activité suspecte détectée. Votre compte a été sécurisé.")
            .build();
    }
    
    /**
     * (PHASE 3) Vérifie si le pays a changé
     * Note: Cette vérification est basée sur les données déjà remplies par SecurityCheckServiceImpl
     */
    public boolean isCountryChanged() {
        if (previousCountry == null || currentCountry == null) {
            return false;
        }
        return !previousCountry.equalsIgnoreCase(currentCountry);
    }
    
    /**
     * (PHASE 3) Vérifie si l'appareil a changé
     * Note: Utilise le DeviceChangeType déjà calculé par DeviceComparisonService
     */
    public boolean isDeviceChanged() {
        return deviceChangeType != null && deviceChangeType != DeviceChangeType.NONE;
    }
    
    /**
     * (PHASE 3) Vérifie si le navigateur a changé
     * Note: Cette vérification est basée sur les données déjà remplies par SecurityCheckServiceImpl
     * Les valeurs proviennent de :
     * - previousBrowser: token.getBrowserName() (stocké en BD)
     * - currentBrowser: currentDeviceInfo.getBrowserName() (connexion actuelle)
     */
    public boolean isBrowserChanged() {
        if (previousBrowser == null || currentBrowser == null) {
            return false;
        }
        return !previousBrowser.equalsIgnoreCase(currentBrowser);
    }
    
    /**
     * (PHASE 3) Vérifie si le système d'exploitation a changé
     * Note: Cette vérification est basée sur les données déjà remplies par SecurityCheckServiceImpl
     * Les valeurs proviennent de :
     * - previousOs: token.getOsName() (stocké en BD)
     * - currentOs: currentDeviceInfo.getOsName() (connexion actuelle)
     */
    public boolean isOsChanged() {
        if (previousOs == null || currentOs == null) {
            return false;
        }
        return !previousOs.equalsIgnoreCase(currentOs);
    }
    
    /**
     * (PHASE 3) Vérifie s'il y a des changements suspects (VPN, Proxy, etc.)
     */
    public boolean hasSuspiciousActivity() {
        return vpnDetected || proxyDetected;
    }
    
    /**
     * (PHASE 3) Retourne un résumé des changements détectés pour les emails
     * Cette méthode est utilisée pour afficher un résumé dans les templates d'email
     */
    public String getChangesSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (ipChanged) {
            summary.append("IP changée");
        }
        
        if (isCountryChanged()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Pays changé (").append(previousCountry).append(" → ").append(currentCountry).append(")");
        }
        
        if (isDeviceChanged()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Appareil changé (").append(deviceChangeType).append(")");
        }
        
        if (isBrowserChanged()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Navigateur changé (").append(previousBrowser).append(" → ").append(currentBrowser).append(")");
        }
        
        if (isOsChanged()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("OS changé (").append(previousOs).append(" → ").append(currentOs).append(")");
        }
        
        if (vpnDetected) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("VPN détecté");
        }
        
        if (proxyDetected) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("Proxy détecté");
        }
        
        return summary.length() > 0 ? summary.toString() : "Aucun changement détecté";
    }
    
    /**
     * (PHASE 4) Calcule automatiquement l'AlertLevel depuis le SecurityRiskLevel
     */
    public AlertLevel calculateAlertLevel() {
        return AlertLevel.fromSecurityRiskLevel(this.riskLevel);
    }
    
    /**
     * (PHASE 4) Met à jour l'alertLevel depuis le riskLevel
     */
    public void updateAlertLevel() {
        this.alertLevel = calculateAlertLevel();
    }
}
