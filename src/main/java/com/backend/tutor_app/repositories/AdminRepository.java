package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Admin;
import com.backend.tutor_app.model.User;
import com.backend.tutor_app.model.enums.AdminLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * Trouve un admin par l'utilisateur associé
     */
    Optional<Admin> findByUser(User user);
    
    /**
     * Trouve un admin par l'ID de l'utilisateur
     */
    @Query("SELECT a FROM Admin a WHERE a.user.id = :userId")
    Optional<Admin> findByUserId(@Param("userId") Long userId);
    
    /**
     * Trouve tous les admins avec pagination
     */
    Page<Admin> findAll(Pageable pageable);
    
    /**
     * Trouve les admins par niveau
     */
    List<Admin> findByAdminLevel(AdminLevel adminLevel);
    
    /**
     * Trouve les admins par département
     */
    List<Admin> findByDepartment(String department);
    
    /**
     * Trouve les super admins
     */
    @Query("SELECT a FROM Admin a WHERE a.adminLevel = 'SUPER_ADMIN'")
    List<Admin> findSuperAdmins();
    
    /**
     * Trouve les admins actifs (dernière connexion récente)
     */
    @Query("SELECT a FROM Admin a WHERE a.user.lastLogin >= :since")
    List<Admin> findActiveAdmins(@Param("since") LocalDateTime since);
    
    /**
     * Compte le nombre total d'admins
     */
    @Query("SELECT COUNT(a) FROM Admin a")
    long countTotalAdmins();
    
    /**
     * Compte les admins par niveau
     */
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.adminLevel = :level")
    long countByAdminLevel(@Param("level") AdminLevel level);
    
    /**
     * Vérifie si un admin existe par email
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE a.user.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Recherche d'admins par nom, email ou département
     */
    @Query("SELECT a FROM Admin a WHERE " +
           "LOWER(a.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.user.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.department) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Admin> searchAdmins(@Param("query") String query, Pageable pageable);
}
