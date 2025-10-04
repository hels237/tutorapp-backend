package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Parent;
import com.backend.tutor_app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    
    /**
     * Trouve un parent par l'utilisateur associé
     */
    Optional<Parent> findByUser(User user);
    
    /**
     * Trouve un parent par l'ID de l'utilisateur
     */
    @Query("SELECT p FROM Parent p WHERE p.user.id = :userId")
    Optional<Parent> findByUserId(@Param("userId") Long userId);
    
    /**
     * Trouve tous les parents avec pagination
     */
    Page<Parent> findAll(Pageable pageable);
    
    /**
     * Trouve les parents par ville
     */
    List<Parent> findByCity(String city);
    
    /**
     * Trouve les parents par code postal
     */
    List<Parent> findByPostalCode(String postalCode);
    
    /**
     * Compte le nombre total de parents
     */
    @Query("SELECT COUNT(p) FROM Parent p")
    long countTotalParents();
    
    /**
     * Vérifie si un parent existe par email
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Parent p WHERE p.user.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
