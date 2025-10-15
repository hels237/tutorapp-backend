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
     * Trouve un parent par son ID (Parent hérite de User, donc pas de relation user séparée)
     */
    Optional<Parent> findById(Long id);
    
    /**
     * Trouve tous les parents avec pagination
     */
    Page<Parent> findAll(Pageable pageable);
    
    /**
     * Trouve les parents par occupation
     */
    List<Parent> findByOccupation(String occupation);
    
    /**
     * Trouve les parents par type de relation avec les enfants
     */
    List<Parent> findByRelationshipToChildren(String relationship);
    
    /**
     * Trouve les parents par mode de communication préféré
     */
    List<Parent> findByPreferredCommunication(String communication);
    
    /**
     * Compte le nombre total de parents
     */
    @Query("SELECT COUNT(p) FROM Parent p")
    long countTotalParents();
    
    /**
     * Vérifie si un parent existe par email
     * Note: Parent hérite de User, donc on accède directement à email
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Parent p WHERE p.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Recherche de parents par nom ou email
     */
    @Query("SELECT p FROM Parent p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Parent> searchParents(@Param("query") String query, Pageable pageable);
}
