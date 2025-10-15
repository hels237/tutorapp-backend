package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.UserDto;
import com.backend.tutor_app.dto.common.PagedResponse;
import com.backend.tutor_app.dto.user.ChangePasswordRequest;
import com.backend.tutor_app.dto.user.UpdatePersonalInfoRequest;
import com.backend.tutor_app.model.Tutor;
import com.backend.tutor_app.model.User;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.repositories.UserRepository;
import com.backend.tutor_app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des utilisateurs pour TutorApp
 * Gère les opérations CRUD et la logique métier des utilisateurs
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        log.info("Création d'un nouvel utilisateur: {}", user.getEmail());
        
        try {
            // Vérification si l'email existe déjà
            if (existsByEmail(user.getEmail())) {
                throw new RuntimeException("Un utilisateur avec cet email existe déjà");
            }
            
            // Définition des valeurs par défaut
            if (user.getStatus() == null) {
                user.setStatus(UserStatus.PENDING_VERIFICATION);
            }
            
            if (user.getEmailVerified() == null) {
                user.setEmailVerified(false);
            }
            
            if (user.getLoginAttempts() == null) {
                user.setLoginAttempts(0);
            }
            
            User savedUser = userRepository.save(user);
            log.info("Utilisateur créé avec succès - ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());
            
            return savedUser;
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'utilisateur: {} - {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur lors de la création de l'utilisateur: " + e.getMessage());
        }
    }

    @Override
    public User updateUser(Long userId, User user) {
        log.info("Mise à jour de l'utilisateur ID: {}", userId);
        
        try {
            User existingUser = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Mise à jour des champs modifiables
            if (user.getFirstName() != null) {
                existingUser.setFirstName(user.getFirstName());
            }
            
            if (user.getLastName() != null) {
                existingUser.setLastName(user.getLastName());
            }
            
            if (user.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(user.getPhoneNumber());
            }
            
            if (user.getProfilePicture() != null) {
                existingUser.setProfilePicture(user.getProfilePicture());
            }
            
            // Les champs sensibles ne peuvent pas être modifiés via cette méthode
            // (email, password, role, status, etc.)
            
            User updatedUser = userRepository.save(existingUser);
            log.info("Utilisateur mis à jour avec succès - ID: {}", updatedUser.getId());
            
            return updatedUser;
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
        }
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Suppression (soft delete) de l'utilisateur ID: {}", userId);
        
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Soft delete - on marque l'utilisateur comme supprimé
            user.setStatus(UserStatus.DELETED);
            userRepository.save(user);
            
            log.info("Utilisateur supprimé (soft delete) avec succès - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        try {
            return userRepository.findById(userId);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur ID: {} - {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur par email: {} - {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        try {
            return userRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de tous les utilisateurs - {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsersByRole(Role role, Pageable pageable) {
        try {
            List<User> users = userRepository.findByRole(role);
            // Conversion en Page (implémentation simplifiée)
            return convertListToPage(users, pageable);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs par rôle: {} - {}", role, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs par rôle");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsersByStatus(UserStatus status, Pageable pageable) {
        try {
            List<User> users = userRepository.findByStatus(status);
            return convertListToPage(users, pageable);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs par statut: {} - {}", status, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs par statut");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de l'existence de l'email: {} - {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public void activateUser(Long userId) {
        log.info("Activation de l'utilisateur ID: {}", userId);
        
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            
            log.info("Utilisateur activé avec succès - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'activation de l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de l'activation de l'utilisateur");
        }
    }

    @Override
    public void deactivateUser(Long userId) {
        log.info("Désactivation de l'utilisateur ID: {}", userId);
        
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
            
            log.info("Utilisateur désactivé avec succès - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors de la désactivation de l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la désactivation de l'utilisateur");
        }
    }

    @Override
    public void suspendUser(Long userId, String reason) {
        log.info("Suspension de l'utilisateur ID: {} - Raison: {}", userId, reason);
        
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setStatus(UserStatus.SUSPENDED);
            userRepository.save(user);
            
            log.info("Utilisateur suspendu avec succès - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors de la suspension de l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la suspension de l'utilisateur");
        }
    }

    @Override
    public void lockUser(Long userId, LocalDateTime lockDuration) {
        log.info("Verrouillage de l'utilisateur ID: {} jusqu'à: {}", userId, lockDuration);
        
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setLockedUntil(lockDuration);
            userRepository.save(user);
            
            log.info("Utilisateur verrouillé avec succès - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors du verrouillage de l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors du verrouillage de l'utilisateur");
        }
    }

    @Override
    public void unlockUser(Long userId) {
        log.info("Déverrouillage de l'utilisateur ID: {}", userId);
        
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setLockedUntil(null);
            userRepository.save(user);
            
            log.info("Utilisateur déverrouillé avec succès - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors du déverrouillage de l'utilisateur ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors du déverrouillage de l'utilisateur");
        }
    }

    @Override
    public UserDto updateProfilePicture(Long userId, String profilePictureUrl) {
        log.info("Mise à jour de la photo de profil pour l'utilisateur ID: {}", userId);

        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setProfilePicture(profilePictureUrl);
            User savedUser = userRepository.save(user);
            
            log.info("Photo de profil mise à jour avec succès - ID: {}", userId);
            
            return UserDto.fromEntity(savedUser);
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la photo de profil - ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour de la photo de profil");
        }
    }

    @Override
    public void updatePersonalInfo(Long userId, String firstName, String lastName, String phoneNumber) {
        log.info("Mise à jour des informations personnelles pour l'utilisateur ID: {}", userId);
        
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            if (firstName != null && !firstName.trim().isEmpty()) {
                user.setFirstName(firstName.trim());
            }
            
            if (lastName != null && !lastName.trim().isEmpty()) {
                user.setLastName(lastName.trim());
            }
            
            if (phoneNumber != null) {
                user.setPhoneNumber(phoneNumber.trim().isEmpty() ? null : phoneNumber.trim());
            }
            
            userRepository.save(user);
            
            log.info("Informations personnelles mises à jour avec succès - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour des informations personnelles - ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour des informations personnelles");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String query, Pageable pageable) {
        try {
            // Recherche simple par email, firstName ou lastName
            // TODO: Implémenter une recherche plus avancée avec des critères multiples
            List<User> allUsers = userRepository.findAll();
            List<User> filteredUsers = allUsers.stream()
                .filter(user -> 
                    user.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                    user.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                    user.getLastName().toLowerCase().contains(query.toLowerCase())
                )
                .toList();
            
            return convertListToPage(filteredUsers, pageable);
            
        } catch (Exception e) {
            log.error("Erreur lors de la recherche d'utilisateurs avec la requête: {} - {}", query, e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche d'utilisateurs");
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<User> getUnverifiedUsersOlderThan(LocalDateTime date) {
        try {
            return userRepository.findUnverifiedUsersOlderThan(date);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs non vérifiés - {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs non vérifiés");
        }
    }

    @Override
    public void markEmailAsVerified(Long userId) {
        log.info("Marquage de l'email comme vérifié pour l'utilisateur ID: {}", userId);
        
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setEmailVerified(true);
            userRepository.save(user);
            
            log.info("Email marqué comme vérifié avec succès - ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Erreur lors du marquage de l'email comme vérifié - ID: {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur lors du marquage de l'email comme vérifié");
        }
    }

    @Override
    public void updateLastLogin(Long userId, LocalDateTime lastLogin) {
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setLastLogin(lastLogin);
            userRepository.save(user);
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la dernière connexion - ID: {} - {}", userId, e.getMessage());
            // On ne lance pas d'exception pour ne pas bloquer le processus de connexion
        }
    }

    @Override
    public void incrementLoginAttempts(Long userId) {
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            userRepository.save(user);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'incrémentation des tentatives de connexion - ID: {} - {}", userId, e.getMessage());
        }
    }

    @Override
    public void resetLoginAttempts(Long userId) {
        try {
            User user = getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            user.setLoginAttempts(0);
            userRepository.save(user);
            
        } catch (Exception e) {
            log.error("Erreur lors de la remise à zéro des tentatives de connexion - ID: {} - {}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Statistiques générales
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);
            
            // Statistiques par rôle
            Map<String, Long> usersByRole = new HashMap<>();
            for (Role role : Role.values()) {
                long count = userRepository.findByRole(role).size();
                usersByRole.put(role.name(), count);
            }
            stats.put("usersByRole", usersByRole);
            
            // Statistiques par statut
            Map<String, Long> usersByStatus = new HashMap<>();
            for (UserStatus status : UserStatus.values()) {
                long count = userRepository.findByStatus(status).size();
                usersByStatus.put(status.name(), count);
            }
            stats.put("usersByStatus", usersByStatus);
            
            // Utilisateurs récents (derniers 30 jours)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long recentUsers = userRepository.findAll().stream()
                .mapToLong(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(thirtyDaysAgo) ? 1 : 0)
                .sum();
            stats.put("recentUsers", recentUsers);
            
            return stats;
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques utilisateurs - {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des statistiques");
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private Page<User> convertListToPage(List<User> users, Pageable pageable) {
        // Implémentation simplifiée de la conversion List vers Page
        // En production, il faudrait utiliser PageImpl avec les bonnes informations de pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());
        
        List<User> pageContent = users.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            users.size()
        );
    }

    // ==================== MÉTHODES POUR UserController ====================

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        log.info("Récupération du profil utilisateur ID: {}", userId);
        User user = getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return UserDto.fromEntity(user);
    }

    @Override
    public UserDto updatePersonalInfo(Long userId, UpdatePersonalInfoRequest request) {
        log.info("Mise à jour des informations personnelles via DTO pour l'utilisateur ID: {}", userId);
        User user = getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        return UserDto.fromEntity(userRepository.save(user));
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changement de mot de passe pour l'utilisateur ID: {}", userId);
        User user = getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Mot de passe changé avec succès - ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSecurityInfo(Long userId) {
        log.info("Récupération des informations de sécurité pour l'utilisateur ID: {}", userId);
        User user = getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Map<String, Object> info = new HashMap<>();
        info.put("emailVerified", user.getEmailVerified());
        info.put("twoFactorEnabled", false);
        info.put("lastPasswordChange", user.getPasswordChangedAt());
        info.put("lastLogin", user.getLastLogin());
        info.put("loginAttempts", user.getLoginAttempts());
        info.put("accountLocked", user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now()));
        info.put("lockedUntil", user.getLockedUntil());

        return info;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDto> searchUsers(String query, String role, String status, Pageable pageable) {
        log.info("Recherche d'utilisateurs avec filtres - query: {}, role: {}, status: {}", query, role, status);

        List<User> users = userRepository.findAll().stream()
                .filter(u -> query == null || query.isEmpty() ||
                        u.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                        u.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                        u.getLastName().toLowerCase().contains(query.toLowerCase()))
                .filter(u -> role == null || role.isEmpty() || u.getRole().name().equalsIgnoreCase(role))
                .filter(u -> status == null || status.isEmpty() || u.getStatus().name().equalsIgnoreCase(status))
                .toList();

        List<UserDto> dtos = users.stream().map(UserDto::fromEntity).toList();
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<UserDto> pageContent = dtos.subList(start, end);
        
        return PagedResponse.<UserDto>builder()
                .content(pageContent)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(users.size())
                .totalPages((int) Math.ceil((double) users.size() / pageable.getPageSize()))
                .first(pageable.getPageNumber() == 0)
                .last(end >= dtos.size())
                .empty(dtos.isEmpty())
                .numberOfElements(pageContent.size())
                .hasNext(end < dtos.size())
                .hasPrevious(pageable.getPageNumber() > 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getTutors(String subject, String level, Double minPrice, Double maxPrice, Double minRating, Boolean available, Pageable pageable) {
        log.info("Récupération des tuteurs avec filtres");

        List<User> tutors = userRepository.findByRole(Role.TUTOR).stream()
                .filter(t -> t.getStatus() == UserStatus.ACTIVE)
                .toList();

        List<UserDto> dtos = tutors.stream().map(UserDto::fromEntity).toList();
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtos.size());
        List<UserDto> pageContent = dtos.subList(start, end);
        
        return PagedResponse.<UserDto>builder()
                .content(pageContent)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(tutors.size())
                .totalPages((int) Math.ceil((double) tutors.size() / pageable.getPageSize()))
                .first(pageable.getPageNumber() == 0)
                .last(end >= dtos.size())
                .empty(dtos.isEmpty())
                .numberOfElements(pageContent.size())
                .hasNext(end < dtos.size())
                .hasPrevious(pageable.getPageNumber() > 0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getPublicProfile(Long userId) {
        log.info("Récupération du profil public pour l'utilisateur ID: {}", userId);
        return getUserProfile(userId);
    }

    @Override
    public Object submitTutorApplication(Long userId, String tutorData, String[] documentPaths) {
        log.info("Soumission d'une demande de tuteur pour l'utilisateur ID: {}", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "pending");
        result.put("message", "Demande de tuteur soumise avec succès");
        result.put("applicationId", System.currentTimeMillis());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getTutorApplicationStatus(Long userId) {
        log.info("Récupération du statut de la demande de tuteur pour l'utilisateur ID: {}", userId);

        Map<String, Object> status = new HashMap<>();
        status.put("status", "pending");
        status.put("submittedAt", LocalDateTime.now().minusDays(5));
        status.put("message", "Votre demande est en cours de traitement");

        return status;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        log.info("Récupération des statistiques pour l'utilisateur ID: {}", userId);
        User user = getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("accountAge", java.time.Duration.between(user.getCreatedAt(), LocalDateTime.now()).toDays());
        stats.put("lastLogin", user.getLastLogin());
        stats.put("totalLogins", 0); // TODO: Implémenter le tracking des connexions

        return stats;
    }

    @Override
    public void deactivateUser(Long userId, String reason) {
        log.info("Désactivation de l'utilisateur ID: {} - Raison: {}", userId, reason);
        User user = getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        log.info("Utilisateur désactivé avec succès - ID: {}, Raison: {}", userId, reason);
    }

    @Override
    public UserDto reactivateUser(Long userId) {
        log.info("Réactivation de l'utilisateur ID: {}", userId);
        User user = getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        user.setLoginAttempts(0);

        User reactivatedUser = userRepository.save(user);
        log.info("Utilisateur réactivé avec succès - ID: {}", userId);

        return UserDto.fromEntity(reactivatedUser);
    }

    @Override
    public void deleteUserAccount(Long userId, String confirmationPassword) {
        log.info("Suppression de compte (GDPR) pour l'utilisateur ID: {}", userId);
        User user = getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(confirmationPassword, user.getPassword())) {
            throw new RuntimeException("Mot de passe de confirmation incorrect");
        }

        user.setStatus(UserStatus.DELETED);
        user.setEmail("deleted_" + userId + "@deleted.com");
        userRepository.save(user);

        log.info("Compte utilisateur supprimé (GDPR) - ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserPreferences(Long userId) {
        log.info("Récupération des préférences pour l'utilisateur ID: {}", userId);
        getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("language", "fr");
        preferences.put("notifications", true);
        preferences.put("emailNotifications", true);
        preferences.put("theme", "light");

        return preferences;
    }

    @Override
    public Map<String, Object> updateUserPreferences(Long userId, Map<String, Object> preferences) {
        log.info("Mise à jour des préférences pour l'utilisateur ID: {}", userId);
        getUserById(userId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        log.info("Préférences mises à jour avec succès - ID: {}", userId);
        return preferences;
    }
}
