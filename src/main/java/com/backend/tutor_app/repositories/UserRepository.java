package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<Utilisateur> findByRole(Role role);
    List<Utilisateur> findByStatus(UserStatus status);
    
    // Méthodes pour les statistiques admin
    Long countByStatus(UserStatus status);
    Long countByRole(Role role);
    Long countByCreatedAtAfter(LocalDateTime date);
    List<Utilisateur> findByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT u FROM Utilisateur u WHERE u.emailVerified = false AND u.createdAt < :date")
    List<Utilisateur> findUnverifiedUsersOlderThan(@Param("date") LocalDateTime date);
}
