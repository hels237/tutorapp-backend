package com.backend.tutor_app.controller;

import com.backend.tutor_app.dto.Auth.UserDto;
import com.backend.tutor_app.dto.common.ApiResponseDto;
import com.backend.tutor_app.dto.common.PagedResponse;
import com.backend.tutor_app.dto.user.UpdatePersonalInfoRequest;
import com.backend.tutor_app.dto.user.ChangePasswordRequest;
import com.backend.tutor_app.services.UserService;
import com.backend.tutor_app.services.FileStorageService;
import com.backend.tutor_app.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller REST pour la gestion des utilisateurs
 * Compatible avec le frontend pour profils, dashboards, etc.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "Gestion des profils utilisateurs")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final TokenService tokenService;

    // ==================== PROFIL UTILISATEUR ====================

    /**
     * GET /api/v1/users/profile
     * Récupère le profil de l'utilisateur connecté - Compatible dashboard
     */
    @GetMapping("/profile")
    @Operation(summary = "Profil utilisateur", description = "Récupère le profil complet de l'utilisateur connecté")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            UserDto userProfile = userService.getUserProfile(userId);
            
            return ResponseEntity.ok(ApiResponseDto.success(userProfile, "Profil utilisateur récupéré"));

        } catch (Exception e) {
            log.error("Erreur récupération profil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Token invalide"));
        }
    }

    /**
     * PUT /api/v1/users/profile
     * Met à jour les informations personnelles - Compatible frontend forms
     */
    @PutMapping("/profile")
    @Operation(summary = "Modifier profil", description = "Met à jour les informations personnelles")
    public ResponseEntity<?> updatePersonalInfo(
            @Valid @RequestBody UpdatePersonalInfoRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            UserDto updatedUser = userService.updatePersonalInfo(userId, request);
            
            return ResponseEntity.ok(ApiResponseDto.success(updatedUser, "Profil mis à jour avec succès"));

        } catch (Exception e) {
            log.error("Erreur mise à jour profil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur mise à jour: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/users/profile/picture
     * Upload photo de profil - Compatible avec le frontend file upload
     */
    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload photo profil", description = "Upload une nouvelle photo de profil")
    public ResponseEntity<?> uploadProfilePicture(
            @Parameter(description = "Fichier image") @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            // Upload du fichier
            String fileName = fileStorageService.uploadProfilePicture(file, userId);
            
            // Mise à jour du profil utilisateur
            UserDto updatedUser = userService.updateProfilePicture(userId, fileName);
            
            return ResponseEntity.ok(ApiResponseDto.success(
                Map.of("user", updatedUser, "fileName", fileName), 
                "Photo de profil mise à jour"
            ));

        } catch (Exception e) {
            log.error("Erreur upload photo profil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur upload: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/users/profile/picture
     * Supprime la photo de profil
     */
    @DeleteMapping("/profile/picture")
    @Operation(summary = "Supprimer photo profil", description = "Supprime la photo de profil actuelle")
    public ResponseEntity<?> deleteProfilePicture(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            UserDto updatedUser = userService.updateProfilePicture(userId, null);
            
            return ResponseEntity.ok(ApiResponseDto.success(updatedUser, "Photo de profil supprimée"));

        } catch (Exception e) {
            log.error("Erreur suppression photo profil: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur suppression photo"));
        }
    }

    // ==================== SÉCURITÉ COMPTE ====================

    /**
     * PUT /api/v1/users/password
     * Changement de mot de passe - Compatible frontend security settings
     */
    @PutMapping("/password")
    @Operation(summary = "Changer mot de passe", description = "Change le mot de passe de l'utilisateur")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            userService.changePassword(userId, request);
            
            return ResponseEntity.ok(ApiResponseDto.success(null, "Mot de passe modifié avec succès"));

        } catch (Exception e) {
            log.error("Erreur changement mot de passe: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur changement mot de passe: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/users/security-info
     * Informations de sécurité du compte
     */
    @GetMapping("/security-info")
    @Operation(summary = "Infos sécurité", description = "Récupère les informations de sécurité du compte")
    public ResponseEntity<?> getSecurityInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            var securityInfo = userService.getSecurityInfo(userId);
            
            return ResponseEntity.ok(ApiResponseDto.success(securityInfo, "Informations de sécurité"));

        } catch (Exception e) {
            log.error("Erreur récupération infos sécurité: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Token invalide"));
        }
    }

    // ==================== RECHERCHE ET LISTING ====================

    /**
     * GET /api/v1/users/search
     * Recherche d'utilisateurs - Compatible avec la page tutors
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher utilisateurs", description = "Recherche d'utilisateurs avec filtres")
    public ResponseEntity<?> searchUsers(
            @Parameter(description = "Terme de recherche") @RequestParam(required = false) String query,
            @Parameter(description = "Rôle") @RequestParam(required = false) String role,
            @Parameter(description = "Statut") @RequestParam(required = false) String status,
            @Parameter(description = "Page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tri") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            PagedResponse<UserDto> users = userService.searchUsers(query, role, status, pageable);
            
            return ResponseEntity.ok(ApiResponseDto.success(users, "Utilisateurs trouvés"));

        } catch (Exception e) {
            log.error("Erreur recherche utilisateurs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur recherche: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/users/tutors
     * Liste des tuteurs - Compatible avec la page /tutors du frontend
     */
    @GetMapping("/tutors")
    @Operation(summary = "Liste tuteurs", description = "Récupère la liste des tuteurs avec filtres")
    public ResponseEntity<?> getTutors(
            @Parameter(description = "Matière") @RequestParam(required = false) String subject,
            @Parameter(description = "Niveau") @RequestParam(required = false) String level,
            @Parameter(description = "Prix min") @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Prix max") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Note min") @RequestParam(required = false) Double minRating,
            @Parameter(description = "Disponible") @RequestParam(required = false) Boolean available,
            @Parameter(description = "Page") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille") @RequestParam(defaultValue = "12") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating"));
            
            PagedResponse<UserDto> tutors = userService.getTutors(
                subject, level, minPrice, maxPrice, minRating, available, pageable
            );
            
            return ResponseEntity.ok(ApiResponseDto.success(tutors, "Tuteurs trouvés"));

        } catch (Exception e) {
            log.error("Erreur récupération tuteurs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur récupération tuteurs"));
        }
    }

    /**
     * GET /api/v1/users/{id}
     * Profil public d'un utilisateur - Compatible avec /tutors/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Profil public", description = "Récupère le profil public d'un utilisateur")
    public ResponseEntity<?> getPublicProfile(@Parameter(description = "ID utilisateur") @PathVariable Long id) {
        try {
            UserDto publicProfile = userService.getPublicProfile(id);
            
            return ResponseEntity.ok(ApiResponseDto.success(publicProfile, "Profil public récupéré"));

        } catch (Exception e) {
            log.error("Erreur récupération profil public: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error("Utilisateur non trouvé"));
        }
    }

    // ==================== DEVENIR TUTEUR ====================

    /**
     * POST /api/v1/users/become-tutor
     * Demande pour devenir tuteur - Compatible avec /become-tutor
     */
    @PostMapping(value = "/become-tutor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Devenir tuteur", description = "Soumet une demande pour devenir tuteur")
    public ResponseEntity<?> becomeTutor(
            @Parameter(description = "Données tuteur") @RequestParam("data") String tutorData,
            @Parameter(description = "Documents") @RequestParam(value = "documents", required = false) MultipartFile[] documents,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            // Upload des documents si fournis
            String[] documentPaths = null;
            if (documents != null && documents.length > 0) {
                documentPaths = fileStorageService.uploadTutorDocuments(documents, userId);
            }
            
            // Traitement de la demande
            var tutorApplication = userService.submitTutorApplication(userId, tutorData, documentPaths);
            
            return ResponseEntity.ok(ApiResponseDto.success(tutorApplication,
                "Demande de tuteur soumise avec succès"));

        } catch (Exception e) {
            log.error("Erreur soumission demande tuteur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur soumission: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/users/tutor-application-status
     * Statut de la demande de tuteur
     */
    @GetMapping("/tutor-application-status")
    @Operation(summary = "Statut demande tuteur", description = "Récupère le statut de la demande de tuteur")
    public ResponseEntity<?> getTutorApplicationStatus(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            var applicationStatus = userService.getTutorApplicationStatus(userId);
            
            return ResponseEntity.ok(ApiResponseDto.success(applicationStatus, "Statut de la demande"));

        } catch (Exception e) {
            log.error("Erreur récupération statut demande: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error("Aucune demande trouvée"));
        }
    }

    // ==================== STATISTIQUES UTILISATEUR ====================

    /**
     * GET /api/v1/users/stats
     * Statistiques de l'utilisateur connecté - Compatible dashboard
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques utilisateur", description = "Récupère les statistiques de l'utilisateur")
    public ResponseEntity<?> getUserStats(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            var userStats = userService.getUserStatistics(userId);
            
            return ResponseEntity.ok(ApiResponseDto.success(userStats, "Statistiques utilisateur"));

        } catch (Exception e) {
            log.error("Erreur récupération statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Token invalide"));
        }
    }

    // ==================== GESTION COMPTE ====================

    /**
     * POST /api/v1/users/deactivate
     * Désactiver le compte utilisateur
     */
    @PostMapping("/deactivate")
    @Operation(summary = "Désactiver compte", description = "Désactive le compte utilisateur")
    public ResponseEntity<?> deactivateAccount(
            @RequestParam(required = false) String reason,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            userService.deactivateUser(userId, reason);
            
            return ResponseEntity.ok(ApiResponseDto.success(null, "Compte désactivé avec succès"));

        } catch (Exception e) {
            log.error("Erreur désactivation compte: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur désactivation: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/users/reactivate
     * Réactiver le compte utilisateur
     */
    @PostMapping("/reactivate")
    @Operation(summary = "Réactiver compte", description = "Réactive le compte utilisateur")
    public ResponseEntity<?> reactivateAccount(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            UserDto reactivatedUser = userService.reactivateUser(userId);
            
            return ResponseEntity.ok(ApiResponseDto.success(reactivatedUser, "Compte réactivé avec succès"));

        } catch (Exception e) {
            log.error("Erreur réactivation compte: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur réactivation: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/users/account
     * Suppression définitive du compte - GDPR compliance
     */
    @DeleteMapping("/account")
    @Operation(summary = "Supprimer compte", description = "Suppression définitive du compte utilisateur")
    public ResponseEntity<?> deleteAccount(
            @RequestParam String confirmationPassword,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            userService.deleteUserAccount(userId, confirmationPassword);
            
            return ResponseEntity.ok(ApiResponseDto.success(null,
                "Compte supprimé définitivement. Toutes vos données ont été effacées."));

        } catch (Exception e) {
            log.error("Erreur suppression compte: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur suppression: " + e.getMessage()));
        }
    }

    // ==================== PRÉFÉRENCES UTILISATEUR ====================

    /**
     * GET /api/v1/users/preferences
     * Récupère les préférences utilisateur
     */
    @GetMapping("/preferences")
    @Operation(summary = "Préférences utilisateur", description = "Récupère les préférences de l'utilisateur")
    public ResponseEntity<?> getUserPreferences(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            var preferences = userService.getUserPreferences(userId);
            
            return ResponseEntity.ok(ApiResponseDto.success(preferences, "Préférences utilisateur"));

        } catch (Exception e) {
            log.error("Erreur récupération préférences: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDto.error("Token invalide"));
        }
    }

    /**
     * PUT /api/v1/users/preferences
     * Met à jour les préférences utilisateur
     */
    @PutMapping("/preferences")
    @Operation(summary = "Modifier préférences", description = "Met à jour les préférences utilisateur")
    public ResponseEntity<?> updateUserPreferences(
            @RequestBody Map<String, Object> preferences,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            Long userId = extractUserIdFromToken(authHeader);
            var updatedPreferences = userService.updateUserPreferences(userId, preferences);
            
            return ResponseEntity.ok(ApiResponseDto.success(updatedPreferences, "Préférences mises à jour"));

        } catch (Exception e) {
            log.error("Erreur mise à jour préférences: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error("Erreur mise à jour préférences"));
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Extrait l'ID utilisateur du token JWT
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return tokenService.getUserIdFromJwtToken(token);
        }
        throw new IllegalArgumentException("Token manquant ou format invalide");
    }
}
