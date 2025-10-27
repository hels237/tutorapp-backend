package com.backend.tutor_app.utils;

import com.backend.tutor_app.dto.Auth.DeviceInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * (Q) PHASE 1 - ÉTAPE 1.2 : Utilitaire pour parser le User Agent et extraire les métadonnées device
 * Parse le User Agent pour identifier le navigateur, l'OS, les versions, etc.
 */
@Component
@Slf4j
public class UserAgentParser {
    
    // (Q) Patterns regex pour détecter les navigateurs
    private static final Pattern CHROME_PATTERN = Pattern.compile("Chrome/([\\d.]+)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("Firefox/([\\d.]+)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("Version/([\\d.]+).*Safari");
    private static final Pattern EDGE_PATTERN = Pattern.compile("Edg/([\\d.]+)");
    private static final Pattern OPERA_PATTERN = Pattern.compile("OPR/([\\d.]+)");
    
    // (Q) Patterns regex pour détecter les OS
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("Windows NT ([\\d.]+)");
    private static final Pattern MAC_PATTERN = Pattern.compile("Mac OS X ([\\d_]+)");
    private static final Pattern LINUX_PATTERN = Pattern.compile("Linux");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("Android ([\\d.]+)");
    private static final Pattern IOS_PATTERN = Pattern.compile("iPhone OS ([\\d_]+)");
    
    /**
     * (Q) Parse le User Agent et retourne un DeviceInfoDto enrichi
     * @param userAgent User Agent complet du navigateur
     * @param ipAddress Adresse IP du client
     * @param timezone Timezone du client (optionnel)
     * @param language Langue du navigateur (optionnel)
     * @return DeviceInfoDto avec toutes les métadonnées extraites
     */
    public DeviceInfoDto parseUserAgent(String userAgent, String ipAddress, String timezone, String language) {
        log.debug("Parsing User Agent: {}", userAgent);
        
        DeviceInfoDto deviceInfo = DeviceInfoDto.builder()
            .userAgent(userAgent)
            .ipAddress(ipAddress)
            .timezone(timezone)
            .browserLanguage(language)
            .build();
        
        if (userAgent == null || userAgent.isBlank()) {
            log.warn("User Agent vide ou null");
            deviceInfo.setBrowserName("Unknown");
            deviceInfo.setOsName("Unknown");
            return deviceInfo;
        }
        
        // (Q) Détection du navigateur
        parseBrowser(userAgent, deviceInfo);
        
        // (Q) Détection de l'OS
        parseOperatingSystem(userAgent, deviceInfo);
        
        log.debug("Device parsed: {} sur {}", deviceInfo.getDeviceSummary(), deviceInfo.getIpAddress());
        
        return deviceInfo;
    }
    
    /**
     * (Q) Parse le navigateur depuis le User Agent
     */
    private void parseBrowser(String userAgent, DeviceInfoDto deviceInfo) {
        // (Q) Ordre important : Edge avant Chrome (car Edge contient "Chrome")
        Matcher edgeMatcher = EDGE_PATTERN.matcher(userAgent);
        if (edgeMatcher.find()) {
            deviceInfo.setBrowserName("Edge");
            deviceInfo.setBrowserVersion(edgeMatcher.group(1));
            return;
        }
        
        // (Q) Opera avant Chrome
        Matcher operaMatcher = OPERA_PATTERN.matcher(userAgent);
        if (operaMatcher.find()) {
            deviceInfo.setBrowserName("Opera");
            deviceInfo.setBrowserVersion(operaMatcher.group(1));
            return;
        }
        
        // (Q) Chrome
        Matcher chromeMatcher = CHROME_PATTERN.matcher(userAgent);
        if (chromeMatcher.find()) {
            deviceInfo.setBrowserName("Chrome");
            deviceInfo.setBrowserVersion(chromeMatcher.group(1));
            return;
        }
        
        // (Q) Firefox
        Matcher firefoxMatcher = FIREFOX_PATTERN.matcher(userAgent);
        if (firefoxMatcher.find()) {
            deviceInfo.setBrowserName("Firefox");
            deviceInfo.setBrowserVersion(firefoxMatcher.group(1));
            return;
        }
        
        // (Q) Safari
        Matcher safariMatcher = SAFARI_PATTERN.matcher(userAgent);
        if (safariMatcher.find()) {
            deviceInfo.setBrowserName("Safari");
            deviceInfo.setBrowserVersion(safariMatcher.group(1));
            return;
        }
        
        // (Q) Navigateur non identifié
        deviceInfo.setBrowserName("Unknown Browser");
        deviceInfo.setBrowserVersion("Unknown");
    }
    
    /**
     * (Q) Parse le système d'exploitation depuis le User Agent
     */
    private void parseOperatingSystem(String userAgent, DeviceInfoDto deviceInfo) {
        // (Q) Windows
        Matcher windowsMatcher = WINDOWS_PATTERN.matcher(userAgent);
        if (windowsMatcher.find()) {
            deviceInfo.setOsName("Windows");
            deviceInfo.setOsVersion(mapWindowsVersion(windowsMatcher.group(1)));
            return;
        }
        
        // (Q) macOS
        Matcher macMatcher = MAC_PATTERN.matcher(userAgent);
        if (macMatcher.find()) {
            deviceInfo.setOsName("macOS");
            deviceInfo.setOsVersion(macMatcher.group(1).replace("_", "."));
            return;
        }
        
        // (Q) iOS
        Matcher iosMatcher = IOS_PATTERN.matcher(userAgent);
        if (iosMatcher.find()) {
            deviceInfo.setOsName("iOS");
            deviceInfo.setOsVersion(iosMatcher.group(1).replace("_", "."));
            return;
        }
        
        // (Q) Android
        Matcher androidMatcher = ANDROID_PATTERN.matcher(userAgent);
        if (androidMatcher.find()) {
            deviceInfo.setOsName("Android");
            deviceInfo.setOsVersion(androidMatcher.group(1));
            return;
        }
        
        // (Q) Linux
        Matcher linuxMatcher = LINUX_PATTERN.matcher(userAgent);
        if (linuxMatcher.find()) {
            deviceInfo.setOsName("Linux");
            deviceInfo.setOsVersion("Unknown");
            return;
        }
        
        // (Q) OS non identifié
        deviceInfo.setOsName("Unknown OS");
        deviceInfo.setOsVersion("Unknown");
    }
    
    /**
     * (Q) Mapping des versions Windows NT vers les noms conviviaux
     */
    private String mapWindowsVersion(String ntVersion) {
        return switch (ntVersion) {
            case "10.0" -> "10/11";
            case "6.3" -> "8.1";
            case "6.2" -> "8";
            case "6.1" -> "7";
            case "6.0" -> "Vista";
            case "5.1" -> "XP";
            default -> ntVersion;
        };
    }
}
