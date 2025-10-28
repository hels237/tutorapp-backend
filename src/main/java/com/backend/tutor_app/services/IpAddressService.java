package com.backend.tutor_app.services;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service dédié à la récupération et validation des adresses IP des clients
 * Gère les différents headers de proxy et reverse proxy
 */
public interface IpAddressService {
    
    /**
     * Récupère l'adresse IP du client depuis le contexte de la requête actuelle
     * Utilise RequestContextHolder pour accéder à la requête HTTP
     * @return Adresse IP du client ou "127.0.0.1" par défaut
     */
    String getClientIp();
    
    /**
     * Extrait l'adresse IP du client depuis une requête HTTP
     * Vérifie les headers standards de proxy dans l'ordre de priorité
     * @param request Requête HTTP
     * @return Adresse IP du client
     */
    String extractClientIpFromRequest(HttpServletRequest request);
    
    /**
     * Valide si une adresse IP est valide (IPv4 ou IPv6)
     * @param ip Adresse IP à valider
     * @return true si l'IP est valide, false sinon
     */
    boolean isValidIp(String ip);
    
    /**
     * Vérifie si une IP est une IP locale/privée
     * @param ip Adresse IP à vérifier
     * @return true si l'IP est locale, false sinon
     */
    boolean isLocalIp(String ip);
}
