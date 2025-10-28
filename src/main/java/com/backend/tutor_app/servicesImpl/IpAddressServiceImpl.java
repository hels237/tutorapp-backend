package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.services.IpAddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * Implémentation du service de récupération d'adresse IP
 * Gère les différents headers de proxy et reverse proxy (Nginx, Apache, CloudFlare, etc.)
 */
@Service
@Slf4j
public class IpAddressServiceImpl implements IpAddressService {
    
    /**
     * Liste des headers HTTP à vérifier pour récupérer l'IP réelle du client
     * Ordre de priorité : du plus fiable au moins fiable
     */
    private static final List<String> IP_HEADERS = Arrays.asList(
        "X-Forwarded-For",          // Standard de facto pour les proxies
        "X-Real-IP",                // Nginx
        "Proxy-Client-IP",          // Apache
        "WL-Proxy-Client-IP",       // WebLogic
        "HTTP_X_FORWARDED_FOR",     // Variante HTTP
        "HTTP_X_FORWARDED",         // Variante HTTP
        "HTTP_X_CLUSTER_CLIENT_IP", // Cluster
        "HTTP_CLIENT_IP",           // Client direct
        "HTTP_FORWARDED_FOR",       // Variante HTTP
        "HTTP_FORWARDED",           // Variante HTTP
        "HTTP_VIA",                 // Via proxy
        "X-CLIENT-IP",              // Variante X-Client
        "CF-Connecting-IP",         // CloudFlare
        "True-Client-IP"            // Akamai, CloudFlare Enterprise
    );
    
    @Override
    public String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String clientIp = extractClientIpFromRequest(request);
                
                log.debug("IP client récupérée: {}", clientIp);
                return clientIp;
            }
            
            log.warn("RequestAttributes null, impossible de récupérer l'IP depuis le contexte");
            
        } catch (Exception e) {
            log.warn("Impossible de récupérer l'IP depuis le contexte: {}", e.getMessage());
        }
        
        return "127.0.0.1"; // Fallback par défaut
    }
    
    @Override
    public String extractClientIpFromRequest(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest null, retour IP par défaut");
            return "127.0.0.1";
        }
        
        // Vérification des headers standards dans l'ordre de priorité
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Pour X-Forwarded-For, prendre le premier IP (client réel)
                // Format: "client, proxy1, proxy2"
                if ("X-Forwarded-For".equals(header) && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                
                // Validation de l'IP avant de la retourner
                if (isValidIp(ip)) {
                    log.debug("IP trouvée via header {}: {}", header, ip);
                    return ip;
                }
            }
        }
        
        // Fallback sur l'adresse distante (connexion directe sans proxy)
        String remoteAddr = request.getRemoteAddr();
        log.debug("IP récupérée via RemoteAddr: {}", remoteAddr);
        
        return remoteAddr != null ? remoteAddr : "127.0.0.1";
    }
    
    @Override
    public boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        
        // Validation IPv4 : format xxx.xxx.xxx.xxx
        if (ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            // Vérification que chaque octet est entre 0 et 255
            String[] octets = ip.split("\\.");
            for (String octet : octets) {
                int value = Integer.parseInt(octet);
                if (value < 0 || value > 255) {
                    return false;
                }
            }
            return true;
        }
        
        // Validation IPv6 : format hexadécimal avec ':'
        if (ip.matches("^[0-9a-fA-F:]+$")) {
            // Validation basique IPv6 (peut être améliorée)
            return ip.contains(":");
        }
        
        return false;
    }
    
    @Override
    public boolean isLocalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IPv4 locales
        if (ip.equals("127.0.0.1") || 
            ip.equals("localhost") ||
            ip.startsWith("192.168.") ||
            ip.startsWith("10.") ||
            ip.startsWith("172.16.") ||
            ip.startsWith("172.17.") ||
            ip.startsWith("172.18.") ||
            ip.startsWith("172.19.") ||
            ip.startsWith("172.20.") ||
            ip.startsWith("172.21.") ||
            ip.startsWith("172.22.") ||
            ip.startsWith("172.23.") ||
            ip.startsWith("172.24.") ||
            ip.startsWith("172.25.") ||
            ip.startsWith("172.26.") ||
            ip.startsWith("172.27.") ||
            ip.startsWith("172.28.") ||
            ip.startsWith("172.29.") ||
            ip.startsWith("172.30.") ||
            ip.startsWith("172.31.")) {
            return true;
        }
        
        // IPv6 locales
        if (ip.equals("::1") || 
            ip.equals("0:0:0:0:0:0:0:1") ||
            ip.startsWith("fe80:") ||
            ip.startsWith("fc00:") ||
            ip.startsWith("fd00:")) {
            return true;
        }
        
        return false;
    }
}
