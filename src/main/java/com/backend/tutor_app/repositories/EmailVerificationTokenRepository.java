package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.support.EmailVerificationToken;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    List<EmailVerificationToken> findByUtilisateurAndVerifiedAtIsNull(Utilisateur utilisateur);

    
/*
    Par défaut, une requête annotée avec @Query est considérée comme une requête SELECT.
    @Modifying dit à Spring Data JPA : attention, cette requête modifie la base de données
    (insert, update ou delete), pas un simple select.
 */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.expiresAt < :date")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);
}
