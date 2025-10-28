package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.Auth.DeviceInfoDto;
import com.backend.tutor_app.dto.Auth.SecurityCheckResult;
import com.backend.tutor_app.model.support.RefreshToken;

/**
 * (Q) PHASE 2 - Service principal de vérification de sécurité
 * Orchestre toutes les vérifications (IP, Device, Token révoqué)
 */
public interface SecurityCheckService {
    
    /**
     * (Q) PHASE 2 - ÉTAPE 2.2/2.3/2.4/2.5 : Effectue toutes les vérifications de sécurité
     * @param token Token à vérifier
     * @param currentDeviceInfo Device actuel
     * @return Résultat complet des vérifications
     */
    SecurityCheckResult performSecurityChecks(RefreshToken token, DeviceInfoDto currentDeviceInfo);
}
