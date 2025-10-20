package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.support.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Trouve un token par sa valeur
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Trouve tous les tokens d'un utilisateur
     */
    List<PasswordResetToken> findByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Trouve tous les tokens actifs d'un utilisateur (non expirés et non utilisés)
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.utilisateur = :user AND t.expiresAt > :now AND t.usedAt IS NULL")
    List<PasswordResetToken> findActiveTokensByUser(@Param("user") Utilisateur utilisateur, @Param("now") LocalDateTime now);
    
    /**
     * Trouve un token actif par sa valeur
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.expiresAt > :now AND t.usedAt IS NULL")
    Optional<PasswordResetToken> findActiveToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Trouve tous les tokens expirés
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.expiresAt <= :now")
    List<PasswordResetToken> findExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Vérifie si un token existe et est valide
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM PasswordResetToken t " +
           "WHERE t.token = :token AND t.expiresAt > :now AND t.usedAt IS NULL")
    boolean existsValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Compte les tokens actifs pour un utilisateur
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.utilisateur = :user AND t.expiresAt > :now AND t.usedAt IS NULL")
    long countActiveTokensByUser(@Param("user") Utilisateur utilisateur, @Param("now") LocalDateTime now);
    
    /**
     * Marque un token comme utilisé
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :usedAt WHERE t.token = :token")
    int markTokenAsUsed(@Param("token") String token, @Param("usedAt") LocalDateTime usedAt);
    
    /**
     * Supprime tous les tokens d'un utilisateur
     */
    @Modifying
    @Transactional
    void deleteByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Supprime tous les tokens expirés
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt <= :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Révoque tous les tokens actifs d'un utilisateur
     */
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :revokedAt " +
           "WHERE t.utilisateur = :user AND t.expiresAt > :now AND t.usedAt IS NULL")
    int revokeAllActiveTokensForUser(@Param("user") Utilisateur utilisateur, @Param("now") LocalDateTime now, @Param("revokedAt") LocalDateTime revokedAt);
    
    /**
     * Trouve les tokens créés dans une période donnée
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.createdAt BETWEEN :start AND :end")
    List<PasswordResetToken> findTokensCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
