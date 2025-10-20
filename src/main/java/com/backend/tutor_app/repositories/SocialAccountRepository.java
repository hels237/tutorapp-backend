package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.SocialProvider;
import com.backend.tutor_app.model.support.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    
    /**
     * Trouve un compte social par provider et provider ID
     */
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);
    
    /**
     * Trouve tous les comptes sociaux d'un utilisateur
     */
    List<SocialAccount> findByUtilisateur(Utilisateur utilisateur);
    
    /**
     * Trouve un compte social spécifique d'un utilisateur
     */
    Optional<SocialAccount> findByUtilisateurAndProvider(Utilisateur utilisateur, SocialProvider provider);
    
    /**
     * Trouve tous les comptes par provider
     */
    List<SocialAccount> findByProvider(SocialProvider provider);
    
    /**
     * Vérifie si un utilisateur a un compte social pour un provider donné
     */
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM SocialAccount sa " +
           "WHERE sa.utilisateur = :utilisateur AND sa.provider = :provider")
    boolean existsByUtilisateurAndProvider(@Param("utilisateur") Utilisateur utilisateur, @Param("provider") SocialProvider provider);
    
    /**
     * Vérifie si un compte social existe pour un provider et provider ID
     */
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM SocialAccount sa " +
           "WHERE sa.provider = :provider AND sa.providerId = :providerId")
    boolean existsByProviderAndProviderId(@Param("provider") SocialProvider provider, @Param("providerId") String providerId);
    
    /**
     * Trouve les comptes sociaux par email du provider
     */
    List<SocialAccount> findByProviderEmail(String providerEmail);
    
    /**
     * Trouve les comptes sociaux créés récemment
     */
    @Query("SELECT sa FROM SocialAccount sa WHERE sa.createdAt >= :since ORDER BY sa.createdAt DESC")
    List<SocialAccount> findRecentlyCreated(@Param("since") LocalDateTime since);
    
    /**
     * Compte les comptes sociaux par provider
     */
    @Query("SELECT COUNT(sa) FROM SocialAccount sa WHERE sa.provider = :provider")
    long countByProvider(@Param("provider") SocialProvider provider);
    
    /**
     * Compte le nombre total de comptes sociaux
     */
    @Query("SELECT COUNT(sa) FROM SocialAccount sa")
    long countTotalSocialAccounts();
    
    /**
     * Trouve les comptes sociaux avec des tokens expirés
     */
    @Query("SELECT sa FROM SocialAccount sa WHERE sa.tokenExpiresAt <= :now")
    List<SocialAccount> findWithExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Trouve les utilisateurs ayant plusieurs comptes sociaux
     */
    @Query("SELECT sa.utilisateur FROM SocialAccount sa GROUP BY sa.utilisateur HAVING COUNT(sa) > 1")
    List<Utilisateur> findUsersWithMultipleSocialAccounts();
    
    /**
     * Statistiques des connexions par provider
     */
    @Query("SELECT sa.provider, COUNT(sa) FROM SocialAccount sa GROUP BY sa.provider")
    List<Object[]> getProviderStatistics();
    
    /**
     * Trouve le compte social principal d'un utilisateur
     */
    Optional<SocialAccount> findByUtilisateurAndIsPrimaryTrue(Utilisateur utilisateur);
    
    /**
     * Trouve les comptes sociaux non principaux d'un utilisateur
     */
    List<SocialAccount> findByUtilisateurAndIsPrimaryFalse(Utilisateur utilisateur);
}
