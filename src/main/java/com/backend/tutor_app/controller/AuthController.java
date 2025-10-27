package com.backend.tutor_app.controller;

import com.backend.tutor_app.dto.Auth.AuthRequest;
import com.backend.tutor_app.dto.Auth.*;
import com.backend.tutor_app.dto.common.ApiResponseDto;
import com.backend.tutor_app.services.AuthService;
import com.backend.tutor_app.services.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie; // (Q) PHASE 1 - ÉTAPE 1.3 : Import Cookie
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // (Q) PHASE 1 - ÉTAPE 1.3 : Import HttpServletResponse
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST pour l'authentification TutorApp
 * Endpoints conformes au frontend pour login, register, reset password, etc.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints d'authentification et gestion des comptes")
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    // ==================== AUTHENTIFICATION PRINCIPALE ====================

    /**
     * POST /api/v1/auth/login
     * Connexion utilisateur - Compatible avec le frontend login form
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur avec email/password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connexion réussie"),
        @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
        @ApiResponse(responseCode = "429", description = "Trop de tentatives")
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) { // (Q) PHASE 1 - ÉTAPE 1.3 : Ajout HttpServletResponse
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            log.info("Tentative de connexion pour: {} depuis IP: {}", request.getEmail(), clientIp);

            // Vérification rate limiting
            if (!rateLimitService.isLoginAllowed(clientIp, request.getEmail())) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponseDto.error("Trop de tentatives de connexion. Veuillez réessayer plus tard."));
            }

            // Authentification
            AuthResponse authResponse = authService.login(request);
            
            // (Q) PHASE 1 - ÉTAPE 1.3 : Stockage sécurisé du Refresh Token dans HttpOnly Cookie
            setRefreshTokenCookie(httpResponse, authResponse.getRefreshToken());
            
            // (Q) PHASE 1 - ÉTAPE 1.3 : Retourner l'Access Token dans le body (sans le Refresh Token)
            AuthResponse responseWithoutRefreshToken = AuthResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .refreshToken(null) // (Q) Ne pas exposer le Refresh Token dans le body
                .tokenType(authResponse.getTokenType())
                .expiresIn(authResponse.getExpiresIn())
                .user(authResponse.getUser())
                .build();
            
            return ResponseEntity.ok(ApiResponseDto.success(responseWithoutRefreshToken, "Connexion réussie"));

        } catch (Exception e) {
            log.error("Erreur lors de la connexion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Email ou mot de passe incorrect"));
        }
    }

    /**
     * POST /api/v1/auth/register
     * Inscription utilisateur
     */
    @PostMapping("/register")
    @Operation(summary = "Inscription utilisateur", description = "Crée un nouveau compte utilisateur")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) { // (Q) PHASE 1 - ÉTAPE 1.3 : Ajout HttpServletResponse

        try {

            // Objectif : empêcher qu’un même utilisateur (ou une même IP) spamme l’inscription.
            String clientIp = getClientIpAddress(httpRequest);
            log.info("Tentative d'inscription pour: {} depuis IP: {}", request.getEmail(), clientIp);
            /*
            * Vérification rate limiting : vérifie si cette IP n’a pas dépassé la limite autorisée d’inscriptions récentes.
            * Si c’est le cas → on bloque l’opération avec une exception.
            * */

            if (!rateLimitService.isRegistrationAllowed(clientIp)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponseDto.error("Trop de tentatives d'inscription. Veuillez réessayer plus tard."));
            }

            // Inscription
            AuthResponse authResponse = authService.register(request);

            // (Q) PHASE 1 - ÉTAPE 1.3 : Stockage sécurisé du Refresh Token dans HttpOnly Cookie
            setRefreshTokenCookie(httpResponse, authResponse.getRefreshToken());
            
            // (Q) PHASE 1 - ÉTAPE 1.3 : Retourner l'Access Token dans le body (sans le Refresh Token)
            AuthResponse responseWithoutRefreshToken = AuthResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .refreshToken(null) // (Q) Ne pas exposer le Refresh Token dans le body
                .tokenType(authResponse.getTokenType())
                .expiresIn(authResponse.getExpiresIn())
                .user(authResponse.getUser())
                .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(responseWithoutRefreshToken, "Inscription réussie. Vérifiez votre email."));

        } catch (Exception e) {
            log.error("Erreur lors de l'inscription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur lors de l'inscription: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/auth/logout
     * Déconnexion utilisateur
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion utilisateur", description = "Déconnecte l'utilisateur et révoque les tokens")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletResponse httpResponse) { // (Q) PHASE 1 - ÉTAPE 1.3 : Ajout HttpServletResponse
        try {
            String token = extractTokenFromHeader(authHeader);
            authService.logout(token);
            
            // (Q) PHASE 1 - ÉTAPE 1.3 : Supprimer le Refresh Token cookie
            clearRefreshTokenCookie(httpResponse);
            
            return ResponseEntity.ok(ApiResponseDto.success(null, "Déconnexion réussie"));

        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur lors de la déconnexion"));
        }
    }

    // ==================== GESTION DES TOKENS ====================

    /**
     * POST /api/v1/auth/refresh
     * (Q) PHASE 1 - ÉTAPE 1.3 : Renouvellement du token JWT avec refresh token depuis HttpOnly Cookie
     */
    @PostMapping("/refresh")
    @Operation(summary = "Renouveler token", description = "Génère un nouveau JWT avec le refresh token depuis le cookie")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) { // (Q) PHASE 1 - ÉTAPE 1.3 : Lecture depuis cookie
        try {
            // (Q) PHASE 1 - ÉTAPE 1.3 : Extraire le Refresh Token depuis le HttpOnly Cookie
            String refreshToken = extractRefreshTokenFromCookie(httpRequest);
            
            if (refreshToken == null || refreshToken.isBlank()) {
                log.warn("(Q) PHASE 1 - Tentative de refresh sans cookie");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("Refresh token manquant"));
            }
            
            AuthResponse authResponse = authService.refreshToken(refreshToken);
            
            // (Q) PHASE 1 - ÉTAPE 1.3 : Mettre à jour le cookie avec le nouveau token (si rotation activée)
            if (authResponse.getRefreshToken() != null) {
                setRefreshTokenCookie(httpResponse, authResponse.getRefreshToken());
            }
            
            // (Q) PHASE 1 - ÉTAPE 1.3 : Retourner uniquement l'Access Token
            AuthResponse responseWithoutRefreshToken = AuthResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .refreshToken(null) // (Q) Ne pas exposer le Refresh Token
                .tokenType(authResponse.getTokenType())
                .expiresIn(authResponse.getExpiresIn())
                .user(authResponse.getUser())
                .build();
            
            return ResponseEntity.ok(ApiResponseDto.success(responseWithoutRefreshToken, "Token renouvelé"));

        } catch (Exception e) {
            log.error("Erreur renouvellement token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Token de rafraîchissement invalide"));
        }
    }

    // ==================== GESTION EMAIL ====================

    /**
     * POST /api/v1/auth/forgot-password
     * Demande de réinitialisation de mot de passe - Compatible frontend forgot-password
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Mot de passe oublié", description = "Envoie un email de réinitialisation")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String clientIp = getClientIpAddress(httpRequest);

            // Vérification rate limiting
            if (!rateLimitService.isPasswordResetAllowed(clientIp, request.getEmail())) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponseDto.error("Trop de demandes de réinitialisation. Veuillez réessayer plus tard."));
            }

            authService.sendPasswordReset(request.getEmail());
            
            return ResponseEntity.ok(ApiResponseDto.success(null,
                "Si cet email existe, vous recevrez un lien de réinitialisation"));

        } catch (Exception e) {
            log.error("Erreur forgot password: {}", e.getMessage());
            // Ne pas révéler si l'email existe ou non
            return ResponseEntity.ok(ApiResponseDto.success(null,
                "Si cet email existe, vous recevrez un lien de réinitialisation"));
        }
    }

    /**
     * POST /api/v1/auth/reset-password
     * Réinitialisation effective du mot de passe avec token
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser mot de passe", description = "Réinitialise le mot de passe avec le token reçu")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            
            return ResponseEntity.ok(ApiResponseDto.success(null, "Mot de passe réinitialisé avec succès"));

        } catch (Exception e) {
            log.error("Erreur reset password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Token invalide ou expiré"));
        }
    }

    /**
     * GET /api/v1/auth/verify-email
     * Vérification d'email avec token - Compatible avec les liens email
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Vérifier email", description = "Vérifie l'email avec le token reçu")
    public ResponseEntity<?> verifyEmail(
            @Parameter(description = "Token de vérification") @RequestParam String token) {
        
        try {
            authService.verifyEmail(token);
            
            return ResponseEntity.ok(ApiResponseDto.success(null, "Email vérifié avec succès"));

        } catch (Exception e) {
            log.error("Erreur vérification email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Token de vérification invalide ou expiré"));
        }
    }

    /**
     * POST /api/v1/auth/resend-verification
     * Renvoyer l'email de vérification
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "Renvoyer email de vérification", description = "Renvoie l'email de vérification")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.sendEmailVerification(request.getEmail());
            
            return ResponseEntity.ok(ApiResponseDto.success(null, "Email de vérification envoyé"));

        } catch (Exception e) {
            log.error("Erreur renvoi vérification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur lors de l'envoi de l'email"));
        }
    }

    // ==================== VALIDATION ET VÉRIFICATIONS ====================

    /**
     * GET /api/v1/auth/validate-token
     * Validation d'un token JWT - Pour vérifications frontend
     */
    @GetMapping("/validate-token")
    @Operation(summary = "Valider token", description = "Vérifie la validité d'un token JWT")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            boolean isValid = authService.validateToken(token);
            
            if (isValid) {
                return ResponseEntity.ok(ApiResponseDto.success(true, "Token valide"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("Token invalide"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Token invalide"));
        }
    }

    /**
     * GET /api/v1/auth/me
     * Récupérer les informations de l'utilisateur connecté
     */
    @GetMapping("/me")
    @Operation(summary = "Profil utilisateur", description = "Récupère les infos de l'utilisateur connecté")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractTokenFromHeader(authHeader);
            UserDto userInfo = authService.getCurrentUser(token);
            
            return ResponseEntity.ok(ApiResponseDto.success(userInfo, "Informations utilisateur"));

        } catch (Exception e) {
            log.error("Erreur récupération utilisateur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Token invalide"));
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * (Q) PHASE 1 - ÉTAPE 1.3 : Configure le Refresh Token dans un HttpOnly Cookie sécurisé
     * Protection contre les attaques XSS : JavaScript ne peut pas accéder au cookie
     * 
     * @param response HttpServletResponse pour ajouter le cookie
     * @param refreshToken Le refresh token à stocker
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null) {
            log.warn("(Q) PHASE 1 - Tentative de création d'un cookie avec un refresh token null");
            return;
        }
        
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        
        // (Q) PHASE 1 - ÉTAPE 1.3 : Configuration sécurisée du cookie
        cookie.setHttpOnly(true);  // (Q) JavaScript ne peut PAS y accéder → Protection XSS
        cookie.setSecure(true);    // (Q) Seulement HTTPS → Protection Man-in-the-Middle
        cookie.setPath("/api/v1/auth/refresh"); // (Q) Limité à l'endpoint de refresh uniquement
        cookie.setMaxAge(7 * 24 * 60 * 60); // (Q) 7 jours (même durée que le token)
        cookie.setAttribute("SameSite", "Strict"); // (Q) Protection CSRF
        
        response.addCookie(cookie);
        
        log.debug("(Q) PHASE 1 - Refresh Token stocké dans HttpOnly Cookie sécurisé");
    }
    
    /**
     * (Q) PHASE 1 - ÉTAPE 1.3 : Supprime le Refresh Token cookie (pour logout)
     * 
     * @param response HttpServletResponse pour supprimer le cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(0); // (Q) Expire immédiatement
        cookie.setAttribute("SameSite", "Strict");
        
        response.addCookie(cookie);
        
        log.debug("(Q) PHASE 1 - Refresh Token cookie supprimé");
    }
    
    /**
     * (Q) PHASE 1 - ÉTAPE 1.3 : Extrait le Refresh Token depuis le HttpOnly Cookie
     * 
     * @param request HttpServletRequest pour lire les cookies
     * @return Le refresh token ou null si absent
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies == null) {
            log.debug("(Q) PHASE 1 - Aucun cookie trouvé dans la requête");
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                log.debug("(Q) PHASE 1 - Refresh Token trouvé dans le cookie");
                return cookie.getValue();
            }
        }
        
        log.debug("(Q) PHASE 1 - Cookie refreshToken non trouvé");
        return null;
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
     * Extrait le token JWT du header Authorization
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Token manquant ou format invalide");
    }
}
