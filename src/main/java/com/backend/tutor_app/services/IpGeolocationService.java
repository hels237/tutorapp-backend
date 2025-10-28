package com.backend.tutor_app.services;

import com.backend.tutor_app.model.enums.SecurityRiskLevel;

/**
 * (Q) PHASE 2 - ÉTAPE 2.4 : Service de géolocalisation IP
 * Détermine le pays d'origine d'une IP et calcule le niveau de risque
 */
public interface IpGeolocationService {
    
    /**
     * (Q) PHASE 2 - Obtient le pays d'une adresse IP
     * @param ipAddress Adresse IP à géolocaliser
     * @return Code pays (ISO 3166-1 alpha-2) ou "UNKNOWN"
     */
    String getCountryFromIp(String ipAddress);
    
    /**
     * (Q) PHASE 2 - Vérifie si une IP est un VPN ou Proxy
     * @param ipAddress Adresse IP à vérifier
     * @return true si VPN/Proxy détecté
     */
    boolean isVpnOrProxy(String ipAddress);
    
    /**
     * (Q) PHASE 2 - Vérifie si un pays est dans la liste noire
     * @param countryCode Code pays (ISO 3166-1 alpha-2)
     * @return true si pays à risque
     */
    boolean isHighRiskCountry(String countryCode);
    
    /**
     * (Q) PHASE 2 - Calcule le niveau de risque basé sur le changement d'IP
     * @param previousIp IP précédente
     * @param currentIp IP actuelle
     * @return Niveau de risque calculé
     */
    SecurityRiskLevel calculateIpRiskLevel(String previousIp, String currentIp);
}
