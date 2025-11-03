package com.backend.tutor_app.controller;

import com.backend.tutor_app.dto.common.ApiResponseDto;
import com.backend.tutor_app.services.IpAddressService;
import com.backend.tutor_app.services.SecurityConfirmationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * (PHASE 3 - Priorité 3) Controller pour la confirmation de sécurité
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Slf4j
public class SecurityConfirmationController {

    private final SecurityConfirmationService confirmationService;
    private final IpAddressService ipAddressService;

    /**
     * Confirme un token de sécurité
     * 
     * @param token Token de confirmation
     * @param request HttpServletRequest pour récupérer IP et User-Agent
     * @return Réponse de confirmation
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponseDto<String>> confirmSecurityToken(
            @RequestParam String token,
            HttpServletRequest request) {
        
        try {
            log.info("(PHASE 3 - Priorité 3)  Tentative de confirmation de sécurité");
            
            String confirmationIp = ipAddressService.getClientIp();
            String confirmationUserAgent = request.getHeader("User-Agent");
            
            boolean confirmed = confirmationService.confirmSecurityToken(
                token, 
                confirmationIp, 
                confirmationUserAgent
            );
            
            if (confirmed) {
                log.info("(PHASE 3 - Priorité 3)  Confirmation réussie depuis IP: {}", confirmationIp);
                return ResponseEntity.ok(
                    ApiResponseDto.success(
                        "CONFIRMED",
                        "Votre compte a été débloqué avec succès. Vous pouvez maintenant vous connecter."
                    )
                );
            } else {
                log.warn("(PHASE 3 - Priorité 3)  Échec de confirmation");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseDto.error("Échec de la confirmation. Veuillez réessayer.", 400)
                );
            }
            
        } catch (RuntimeException e) {
            log.error("(PHASE 3 - Priorité 3) ❌ Erreur confirmation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponseDto.error(e.getMessage(), 400)
            );
        } catch (Exception e) {
            log.error("(PHASE 3 - Priorité 3) ❌ Erreur inattendue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponseDto.error("Une erreur est survenue lors de la confirmation", 500)
            );
        }
    }

    /**
     * Vérifie si un utilisateur a une confirmation en attente
     * 
     * @param userId ID de l'utilisateur
     * @return true si confirmation en attente
     */
    @GetMapping("/pending/{userId}")
    public ResponseEntity<ApiResponseDto<Boolean>> hasPendingConfirmation(@PathVariable Long userId) {
        try {
            boolean pending = confirmationService.hasPendingConfirmation(userId);
            return ResponseEntity.ok(
                ApiResponseDto.success(
                    pending,
                    pending ? "Confirmation en attente" : "Aucune confirmation en attente"
                )
            );
        } catch (Exception e) {
            log.error("(PHASE 3 - Priorité 3) ❌ Erreur vérification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponseDto.error("Erreur lors de la vérification", 500)
            );
        }
    }
}
