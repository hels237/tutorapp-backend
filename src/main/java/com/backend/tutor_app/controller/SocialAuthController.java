package com.backend.tutor_app.controller;

import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.dto.Auth.SocialLoginRequest;
import com.backend.tutor_app.dto.common.ApiResponseDto;
import com.backend.tutor_app.model.enums.SocialProvider;
import com.backend.tutor_app.services.SocialAuthService;
import com.backend.tutor_app.services.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST pour l'authentification sociale OAuth2
 * Compatible avec le frontend pour Google, Facebook, GitHub
 */
@RestController
@RequestMapping("/api/v1/auth/social")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Social Authentication", description = "Authentification OAuth2 avec Google, Facebook, GitHub")
public class SocialAuthController {

    private final SocialAuthService socialAuthService;
    private final RateLimitService rateLimitService;

    // ==================== GÉNÉRATION URLS D'AUTORISATION ====================

    /**
     * GET /api/v1/auth/social/google/authorize
     * Génère l'URL d'autorisation Google - Compatible frontend
     */
    @GetMapping("/google/authorize")
    @Operation(summary = "URL autorisation Google", description = "Génère l'URL de redirection vers Google OAuth2")
    public ResponseEntity<?> getGoogleAuthUrl(
            @Parameter(description = "URL de redirection après auth") 
            @RequestParam(defaultValue = "http://localhost:3000/auth/callback/google") String redirectUri) {
        
        try {
            String authUrl = socialAuthService.generateAuthorizationUrl(SocialProvider.GOOGLE, redirectUri);
            
            return ResponseEntity.ok(ApiResponseDto.success(
                Map.of("authUrl", authUrl, "provider", "google"), 
                "URL d'autorisation Google générée"
            ));

        } catch (Exception e) {
            log.error("Erreur génération URL Google: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erreur configuration Google OAuth2"));
        }
    }

    /**
     * GET /api/v1/auth/social/facebook/authorize
     * Génère l'URL d'autorisation Facebook - Compatible frontend
     */
    @GetMapping("/facebook/authorize")
    @Operation(summary = "URL autorisation Facebook", description = "Génère l'URL de redirection vers Facebook OAuth2")
    public ResponseEntity<?> getFacebookAuthUrl(
            @Parameter(description = "URL de redirection après auth") 
            @RequestParam(defaultValue = "http://localhost:3000/auth/callback/facebook") String redirectUri) {
        
        try {
            String authUrl = socialAuthService.generateAuthorizationUrl(SocialProvider.FACEBOOK, redirectUri);
            
            return ResponseEntity.ok(ApiResponseDto.success(
                Map.of("authUrl", authUrl, "provider", "facebook"), 
                "URL d'autorisation Facebook générée"
            ));

        } catch (Exception e) {
            log.error("Erreur génération URL Facebook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erreur configuration Facebook OAuth2"));
        }
    }

    /**
     * GET /api/v1/auth/social/github/authorize
     * Génère l'URL d'autorisation GitHub - Compatible frontend
     */
    @GetMapping("/github/authorize")
    @Operation(summary = "URL autorisation GitHub", description = "Génère l'URL de redirection vers GitHub OAuth2")
    public ResponseEntity<?> getGitHubAuthUrl(
            @Parameter(description = "URL de redirection après auth") 
            @RequestParam(defaultValue = "http://localhost:3000/auth/callback/github") String redirectUri) {
        
        try {
            String authUrl = socialAuthService.generateAuthorizationUrl(SocialProvider.GITHUB, redirectUri);
            
            return ResponseEntity.ok(ApiResponseDto.success(
                Map.of("authUrl", authUrl, "provider", "github"), 
                "URL d'autorisation GitHub générée"
            ));

        } catch (Exception e) {
            log.error("Erreur génération URL GitHub: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("Erreur configuration GitHub OAuth2"));
        }
    }

    // ==================== CALLBACKS OAUTH2 ====================

    /**
     * POST /api/v1/auth/social/google/callback
     * Callback Google OAuth2 - Traite le code d'autorisation
     */
    @PostMapping("/google/callback")
    @Operation(summary = "Callback Google OAuth2", description = "Traite le code d'autorisation Google et authentifie l'utilisateur")
    public ResponseEntity<?> googleCallback(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest) {
        
        return handleSocialCallback(SocialProvider.GOOGLE, request, httpRequest);
    }

    /**
     * POST /api/v1/auth/social/facebook/callback
     * Callback Facebook OAuth2 - Traite le code d'autorisation
     */
    @PostMapping("/facebook/callback")
    @Operation(summary = "Callback Facebook OAuth2", description = "Traite le code d'autorisation Facebook et authentifie l'utilisateur")
    public ResponseEntity<?> facebookCallback(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest) {
        
        return handleSocialCallback(SocialProvider.FACEBOOK, request, httpRequest);
    }

    /**
     * POST /api/v1/auth/social/github/callback
     * Callback GitHub OAuth2 - Traite le code d'autorisation
     */
    @PostMapping("/github/callback")
    @Operation(summary = "Callback GitHub OAuth2", description = "Traite le code d'autorisation GitHub et authentifie l'utilisateur")
    public ResponseEntity<?> githubCallback(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest) {
        
        return handleSocialCallback(SocialProvider.GITHUB, request, httpRequest);
    }

    // ==================== LIAISON COMPTES SOCIAUX (Utilisateur connecté) ====================

    /**
     * POST /api/v1/auth/social/google/link
     * Lie un compte Google à l'utilisateur connecté
     */
    @PostMapping("/google/link")
    @Operation(summary = "Lier compte Google", description = "Lie un compte Google à l'utilisateur connecté")
    public ResponseEntity<?> linkGoogleAccount(
            @Valid @RequestBody SocialLoginRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        return handleSocialLink(SocialProvider.GOOGLE, request, authHeader);
    }

    /**
     * POST /api/v1/auth/social/facebook/link
     * Lie un compte Facebook à l'utilisateur connecté
     */
    @PostMapping("/facebook/link")
    @Operation(summary = "Lier compte Facebook", description = "Lie un compte Facebook à l'utilisateur connecté")
    public ResponseEntity<?> linkFacebookAccount(
            @Valid @RequestBody SocialLoginRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        return handleSocialLink(SocialProvider.FACEBOOK, request, authHeader);
    }

    /**
     * POST /api/v1/auth/social/github/link
     * Lie un compte GitHub à l'utilisateur connecté
     */
    @PostMapping("/github/link")
    @Operation(summary = "Lier compte GitHub", description = "Lie un compte GitHub à l'utilisateur connecté")
    public ResponseEntity<?> linkGitHubAccount(
            @Valid @RequestBody SocialLoginRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        return handleSocialLink(SocialProvider.GITHUB, request, authHeader);
    }

    // ==================== GESTION COMPTES SOCIAUX ====================

    /**
     * GET /api/v1/auth/social/accounts
     * Liste les comptes sociaux liés à l'utilisateur connecté
     */
    @GetMapping("/accounts")
    @Operation(summary = "Comptes sociaux liés", description = "Liste les comptes sociaux de l'utilisateur connecté")
    public ResponseEntity<?> getUserSocialAccounts(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            var socialAccounts = socialAuthService.getUserSocialAccounts(userId);
            
            return ResponseEntity.ok(ApiResponseDto.success(socialAccounts, "Comptes sociaux récupérés"));

        } catch (Exception e) {
            log.error("Erreur récupération comptes sociaux: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Token invalide"));
        }
    }

    /**
     * DELETE /api/v1/auth/social/{provider}/unlink
     * Délie un compte social de l'utilisateur connecté
     */
    @DeleteMapping("/{provider}/unlink")
    @Operation(summary = "Délier compte social", description = "Supprime la liaison avec un compte social")
    public ResponseEntity<?> unlinkSocialAccount(
            @Parameter(description = "Provider à délier") @PathVariable String provider,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            SocialProvider socialProvider = SocialProvider.valueOf(provider.toUpperCase());
            
            socialAuthService.unlinkSocialAccount(userId, socialProvider);
            
            return ResponseEntity.ok(ApiResponseDto.success(null,
                "Compte " + provider + " délié avec succès"));

        } catch (Exception e) {
            log.error("Erreur déliaison compte social: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur lors de la déliaison"));
        }
    }

    // ==================== MÉTHODES UTILITAIRES PRIVÉES ====================

    /**
     * Gère le callback OAuth2 pour tous les providers
     */
    private ResponseEntity<?> handleSocialCallback(SocialProvider provider, SocialLoginRequest request, HttpServletRequest httpRequest) {
        try {
            String clientIp = getClientIpAddress(httpRequest);
            
            // Vérification rate limiting
            if (!rateLimitService.isSocialAuthAllowed(clientIp, provider.name())) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponseDto.error("Trop de tentatives d'authentification sociale"));
            }

            // Authentification sociale
            AuthResponse authResponse = socialAuthService.authenticateWithSocialProvider(provider, request.getCode());
            
            // Enregistrement de la tentative
            rateLimitService.recordSocialAuthAttempt(clientIp, provider.name());
            
            log.info("Authentification sociale réussie avec {} pour l'utilisateur: {}", 
                provider, authResponse.getUserProfileDto().getEmail());
            
            return ResponseEntity.ok(ApiResponseDto.success(authResponse,
                "Connexion " + provider.name() + " réussie"));

        } catch (Exception e) {
            log.error("Erreur authentification sociale {} - {}", provider, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur authentification " + provider.name() + ": " + e.getMessage()));
        }
    }

    /**
     * Gère la liaison d'un compte social pour un utilisateur connecté
     */
    private ResponseEntity<?> handleSocialLink(SocialProvider provider, SocialLoginRequest request, String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            var socialAccount = socialAuthService.linkSocialAccount(userId, provider, request.getCode());
            
            return ResponseEntity.ok(ApiResponseDto.success(socialAccount,
                "Compte " + provider.name() + " lié avec succès"));

        } catch (Exception e) {
            log.error("Erreur liaison compte social {} - {}", provider, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur liaison " + provider.name() + ": " + e.getMessage()));
        }
    }

    /**
     * Extrait l'adresse IP du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Extrait l'ID utilisateur du token JWT
     */
    private Long extractUserIdFromToken(String authHeader) {
        // Cette méthode devrait utiliser le TokenService pour extraire l'ID
        // Pour l'instant, on simule - à implémenter avec le vrai service
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // TODO: Utiliser TokenService.getUserIdFromJwtToken(token)
            return 1L; // Placeholder
        }
        throw new IllegalArgumentException("Token manquant ou format invalide");
    }
}
