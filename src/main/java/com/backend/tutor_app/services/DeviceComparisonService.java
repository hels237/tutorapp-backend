package com.backend.tutor_app.services;

import com.backend.tutor_app.dto.Auth.DeviceInfoDto;
import com.backend.tutor_app.model.enums.DeviceChangeType;

/**
 * (Q) PHASE 2 - ÉTAPE 2.5 : Service de comparaison de devices
 * Analyse les changements de device pour détecter les comportements suspects
 */
public interface DeviceComparisonService {
    
    /**
     * (Q) PHASE 2 - Compare deux devices et détermine le type de changement
     * @param previousDevice Device précédent
     * @param currentDevice Device actuel
     * @return Type de changement détecté
     */
    DeviceChangeType compareDevices(DeviceInfoDto previousDevice, DeviceInfoDto currentDevice);
    
    /**
     * (Q) PHASE 2 - Vérifie si le changement de device est suspect
     * @param changeType Type de changement
     * @return true si le changement est suspect
     */
    boolean isSuspiciousChange(DeviceChangeType changeType);
    
    /**
     * (Q) PHASE 2 - Génère un message descriptif du changement de device
     * @param previousDevice Device précédent
     * @param currentDevice Device actuel
     * @return Message descriptif
     */
    String getChangeDescription(DeviceInfoDto previousDevice, DeviceInfoDto currentDevice);
}
