package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.support.RefreshToken;
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
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Trouve un refresh token par sa valeur
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Trouve tous les refresh tokens d'un utilisateur
     */
    List<RefreshToken> findByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Trouve tous les refresh tokens actifs d'un utilisateur (non expirés et non révoqués)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.utilisateur = :user AND rt.expiresAt > :now AND rt.isRevoked = false")
    List<RefreshToken> findActiveTokensByUser(@Param("user") Utilisateur utilisateur, @Param("now") LocalDateTime now);
    
    /**
     * Trouve un refresh token actif par sa valeur
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.expiresAt > :now AND rt.isRevoked = false")
    Optional<RefreshToken> findActiveToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Trouve tous les refresh tokens expirés
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt <= :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Trouve tous les refresh tokens révoqués
     */
    List<RefreshToken> findByIsRevokedTrue();
    
    /**
     * Trouve tous les refresh tokens non révoqués
     */
    List<RefreshToken> findByIsRevokedFalse();
    
    /**
     * Vérifie si un refresh token existe et est valide
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt " +
           "WHERE rt.token = :token AND rt.expiresAt > :now AND rt.isRevoked = false")
    boolean existsValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Compte les refresh tokens actifs pour un utilisateur
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.utilisateur = :utilisateur AND rt.expiresAt > :now AND rt.isRevoked = false")
    long countActiveTokensByUser(@Param("user") Utilisateur utilisateur, @Param("now") LocalDateTime now);
    
    /**
     * Révoque un refresh token
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.token = :token")
    int revokeToken(@Param("token") String token);
    
    /**
     * Révoque tous les refresh tokens d'un utilisateur
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.utilisateur = :user AND rt.isRevoked = false")
    int revokeAllUserTokens(@Param("user") Utilisateur utilisateur);
    
    /**
     * Supprime tous les refresh tokens d'un utilisateur
     */
    @Modifying
    @Transactional
    void deleteByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Supprime tous les refresh tokens expirés
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt <= :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Supprime tous les refresh tokens révoqués plus anciens que la date donnée
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true AND rt.createdAt <= :before")
    int deleteRevokedTokensOlderThan(@Param("before") LocalDateTime before);
    
    /**
     * Trouve les refresh tokens par adresse IP
     */
    List<RefreshToken> findByIpAddress(String ipAddress);
    
    /**
     * Trouve les refresh tokens créés dans une période donnée
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.createdAt BETWEEN :start AND :end")
    List<RefreshToken> findTokensCreatedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Met à jour la date de dernière utilisation d'un token
     */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.lastUsed = :lastUsed WHERE rt.token = :token")
    int updateLastUsed(@Param("token") String token, @Param("lastUsed") LocalDateTime lastUsed);
    
    /**
     * Nettoie les anciens tokens (expirés et révoqués depuis plus de X jours)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE " +
           "(rt.expiresAt <= :expiredBefore) OR " +
           "(rt.isRevoked = true AND rt.createdAt <= :revokedBefore)")
    int cleanupOldTokens(@Param("expiredBefore") LocalDateTime expiredBefore, @Param("revokedBefore") LocalDateTime revokedBefore);
}
