package com.backend.tutor_app.dto.Auth;

import lombok.*;

/**
 * (Q) PHASE 1 - ÉTAPE 1.2 : DTO pour collecter les métadonnées enrichies du device
 * Utilisé pour améliorer la sécurité et la traçabilité des connexions
 */
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoDto {
    
    // (Q) Informations du navigateur
    private String browserName;        // Ex: "Chrome", "Firefox", "Safari"
    private String browserVersion;     // Ex: "120.0.6099.109"
    
    // (Q) Informations du système d'exploitation
    private String osName;             // Ex: "Windows", "macOS", "Linux", "Android", "iOS"
    private String osVersion;          // Ex: "10.0", "14.2.1"
    
    // (Q) Informations de localisation et langue
    private String timezone;           // Ex: "Europe/Paris", "America/New_York"
    private String browserLanguage;    // Ex: "fr-FR", "en-US", "es-ES"
    
    // (Q) User Agent complet pour analyse détaillée
    private String userAgent;          // User Agent complet du navigateur
    
    // (Q) Adresse IP de la requête
    private String ipAddress;          // Adresse IP du client
    
    /**
     * (Q) Méthode utilitaire pour créer un résumé lisible du device
     * Utilisé pour les logs et les notifications utilisateur
     */
    public String getDeviceSummary() {
        return String.format("%s %s sur %s %s", 
            browserName != null ? browserName : "Unknown Browser",
            browserVersion != null ? browserVersion : "",
            osName != null ? osName : "Unknown OS",
            osVersion != null ? osVersion : ""
        ).trim();
    }
    
    /**
     * (Q) Méthode pour obtenir une description complète du device
     * Utilisé pour stocker dans deviceInfo (compatibilité avec l'ancien format)
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        
        if (browserName != null) {
            sb.append(browserName);
            if (browserVersion != null) {
                sb.append(" ").append(browserVersion);
            }
        }
        
        if (osName != null) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(osName);
            if (osVersion != null) {
                sb.append(" ").append(osVersion);
            }
        }
        
        if (timezone != null) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append("TZ: ").append(timezone);
        }
        
        if (browserLanguage != null) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append("Lang: ").append(browserLanguage);
        }
        
        return sb.toString();
    }
    
    /**
     * (Q) Validation basique des données
     */
    public boolean isValid() {
        return userAgent != null && !userAgent.isBlank();
    }
}
