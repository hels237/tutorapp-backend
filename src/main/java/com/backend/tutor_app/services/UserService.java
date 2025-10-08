package com.backend.tutor_app.services;

import com.backend.tutor_app.model.User;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des utilisateurs pour TutorApp
 * Gère les opérations CRUD et la logique métier des utilisateurs
 */
public interface UserService {
    
    /**
     * Crée un nouvel utilisateur
     * @param user Données de l'utilisateur à créer
     * @return Utilisateur créé
     */
    User createUser(User user);
    
    /**
     * Met à jour un utilisateur existant
     * @param userId ID de l'utilisateur
     * @param user Nouvelles données de l'utilisateur
     * @return Utilisateur mis à jour
     */
    User updateUser(Long userId, User user);
    
    /**
     * Supprime un utilisateur (soft delete)
     * @param userId ID de l'utilisateur à supprimer
     */
    void deleteUser(Long userId);
    
    /**
     * Récupère un utilisateur par son ID
     * @param userId ID de l'utilisateur
     * @return Utilisateur trouvé
     */
    Optional<User> getUserById(Long userId);
    
    /**
     * Récupère un utilisateur par son email
     * @param email Email de l'utilisateur
     * @return Utilisateur trouvé
     */
    Optional<User> getUserByEmail(String email);
    
    /**
     * Récupère tous les utilisateurs avec pagination
     * @param pageable Paramètres de pagination
     * @return Page d'utilisateurs
     */
    Page<User> getAllUsers(Pageable pageable);
    
    /**
     * Récupère les utilisateurs par rôle
     * @param role Rôle des utilisateurs
     * @param pageable Paramètres de pagination
     * @return Page d'utilisateurs avec le rôle spécifié
     */
    Page<User> getUsersByRole(Role role, Pageable pageable);
    
    /**
     * Récupère les utilisateurs par statut
     * @param status Statut des utilisateurs
     * @param pageable Paramètres de pagination
     * @return Page d'utilisateurs avec le statut spécifié
     */
    Page<User> getUsersByStatus(UserStatus status, Pageable pageable);
    
    /**
     * Vérifie si un email existe déjà
     * @param email Email à vérifier
     * @return true si l'email existe
     */
    boolean existsByEmail(String email);
    
    /**
     * Active un utilisateur
     * @param userId ID de l'utilisateur à activer
     */
    void activateUser(Long userId);
    
    /**
     * Désactive un utilisateur
     * @param userId ID de l'utilisateur à désactiver
     */
    void deactivateUser(Long userId);
    
    /**
     * Suspend un utilisateur
     * @param userId ID de l'utilisateur à suspendre
     * @param reason Raison de la suspension
     */
    void suspendUser(Long userId, String reason);
    
    /**
     * Verrouille un utilisateur temporairement
     * @param userId ID de l'utilisateur à verrouiller
     * @param lockDuration Durée du verrouillage
     */
    void lockUser(Long userId, LocalDateTime lockDuration);
    
    /**
     * Déverrouille un utilisateur
     * @param userId ID de l'utilisateur à déverrouiller
     */
    void unlockUser(Long userId);
    
    /**
     * Met à jour la photo de profil d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param profilePictureUrl URL de la nouvelle photo de profil
     */
    void updateProfilePicture(Long userId, String profilePictureUrl);
    
    /**
     * Met à jour les informations personnelles d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param firstName Nouveau prénom
     * @param lastName Nouveau nom
     * @param phoneNumber Nouveau numéro de téléphone
     */
    void updatePersonalInfo(Long userId, String firstName, String lastName, String phoneNumber);
    
    /**
     * Recherche des utilisateurs par nom ou email
     * @param query Terme de recherche
     * @param pageable Paramètres de pagination
     * @return Page d'utilisateurs correspondant à la recherche
     */
    Page<User> searchUsers(String query, Pageable pageable);
    
    /**
     * Récupère les utilisateurs non vérifiés plus anciens qu'une date
     * @param date Date limite
     * @return Liste des utilisateurs non vérifiés
     */
    List<User> getUnverifiedUsersOlderThan(LocalDateTime date);
    
    /**
     * Marque l'email d'un utilisateur comme vérifié
     * @param userId ID de l'utilisateur
     */
    void markEmailAsVerified(Long userId);
    
    /**
     * Met à jour la date de dernière connexion
     * @param userId ID de l'utilisateur
     * @param lastLogin Date de dernière connexion
     */
    void updateLastLogin(Long userId, LocalDateTime lastLogin);
    
    /**
     * Incrémente le compteur de tentatives de connexion
     * @param userId ID de l'utilisateur
     */
    void incrementLoginAttempts(Long userId);
    
    /**
     * Remet à zéro le compteur de tentatives de connexion
     * @param userId ID de l'utilisateur
     */
    void resetLoginAttempts(Long userId);
    
    /**
     * Récupère les statistiques des utilisateurs
     * @return Map avec les statistiques (total, actifs, par rôle, etc.)
     */
    java.util.Map<String, Object> getUserStatistics();
}
