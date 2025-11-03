package com.backend.tutor_app.repository;

import com.backend.tutor_app.model.support.SecurityConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * (PHASE 3 - Priorité 3) Repository pour les tokens de confirmation de sécurité
 */
@Repository
public interface SecurityConfirmationTokenRepository extends JpaRepository<SecurityConfirmationToken, Long> {

    /**
     * Trouve un token par sa valeur
     */
    Optional<SecurityConfirmationToken> findByToken(String token);

    /**
     * Trouve tous les tokens non utilisés d'un utilisateur
     */
    @Query("SELECT t FROM SecurityConfirmationToken t WHERE t.utilisateur.id = :userId AND t.isUsed = false")
    List<SecurityConfirmationToken> findUnusedTokensByUserId(Long userId);

    /**
     * Trouve tous les tokens valides (non utilisés et non expirés) d'un utilisateur
     */
    @Query("SELECT t FROM SecurityConfirmationToken t WHERE t.utilisateur.id = :userId " +
           "AND t.isUsed = false AND t.expiresAt > :now")
    List<SecurityConfirmationToken> findValidTokensByUserId(Long userId, LocalDateTime now);

    /**
     * Vérifie si un utilisateur a des tokens de confirmation en attente
     */
    @Query("SELECT COUNT(t) > 0 FROM SecurityConfirmationToken t " +
           "WHERE t.utilisateur.id = :userId AND t.isUsed = false AND t.expiresAt > :now")
    boolean hasPendingConfirmation(Long userId, LocalDateTime now);

    /**
     * Invalide tous les tokens non utilisés d'un utilisateur
     */
    @Modifying
    @Query("UPDATE SecurityConfirmationToken t SET t.isUsed = true " +
           "WHERE t.utilisateur.id = :userId AND t.isUsed = false")
    void invalidateAllUserTokens(Long userId);

    /**
     * Supprime les tokens expirés (nettoyage)
     */
    @Modifying
    @Query("DELETE FROM SecurityConfirmationToken t WHERE t.expiresAt < :expirationDate")
    void deleteExpiredTokens(LocalDateTime expirationDate);

    /**
     * Compte les tokens en attente pour un utilisateur
     */
    @Query("SELECT COUNT(t) FROM SecurityConfirmationToken t " +
           "WHERE t.utilisateur.id = :userId AND t.isUsed = false AND t.expiresAt > :now")
    long countPendingTokens(Long userId, LocalDateTime now);
}
