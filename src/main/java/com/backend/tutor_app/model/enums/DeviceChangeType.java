package com.backend.tutor_app.model.enums;

/**
 * (Q) PHASE 2 - ÉTAPE 2.5 : Types de changement de device détectés
 * Utilisé pour analyser les changements de device et déterminer s'ils sont suspects
 */
public enum DeviceChangeType {
    /**
     * (Q) Aucun changement - Même device exact
     */
    NONE,
    
    /**
     * (Q) Changement mineur - Mise à jour de navigateur/OS
     * Ex: Chrome 120 → Chrome 121
     */
    MINOR,
    
    /**
     * (Q) Changement majeur - Navigateur ou OS complètement différent
     * Ex: Chrome/Windows → Firefox/Linux
     */
    MAJOR,
    
    /**
     * (Q) Changement suspect - Device complètement différent
     * Ex: Desktop → Mobile, ou fingerprint totalement différent
     */
    SUSPICIOUS
}
