package com.backend.tutor_app.dto.Auth;

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
}
