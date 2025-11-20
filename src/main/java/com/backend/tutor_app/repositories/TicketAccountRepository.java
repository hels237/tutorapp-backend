package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.TicketAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'accès et le verrouillage des comptes de tickets.
 * - Fournit des recherches par UUID métier et par identifiant étudiant.
 * - Expose des méthodes avec verrouillage PESSIMISTIC_WRITE pour sécuriser les opérations
 *   atomiques de débit/crédit sur le solde.
 * Usage typique: utilisé par TicketAccountService pour récupérer/locker un compte avant
 * modification du solde, et par les contrôleurs pour la lecture du compte.
 */
@Repository
public interface TicketAccountRepository extends JpaRepository<TicketAccount, Long> {

    /**
     * Récupère un compte via son UUID métier (stable pour intégrations externes).
     * - Use cases: liens REST, diagnostics, intégrations.
     */
    Optional<TicketAccount> findByUuid(UUID uuid);

    /**
     * Récupère le compte unique d'un étudiant via son identifiant interne.
     * - Contrainte: 1 compte par étudiant (contrainte unique en base).
     */
    Optional<TicketAccount> findByStudent_Id(Long studentId);

    /**
     * Verrouille la ligne de compte par ID pour une mise à jour sûre du solde.
     * - À utiliser dans une méthode de service annotée @Transactional.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from TicketAccount a where a.id = :id")
    Optional<TicketAccount> lockById(@Param("id") Long id);

    /**
     * Verrouille la ligne de compte via l'ID de l'étudiant.
     * - Préféré quand on part d'un contexte métier 'studentId'.
     * @Lock(LockModeType.PESSIMISTIC_WRITE) sert à verrouiller une ligne en base de données pour empêcher deux requêtes simultanées d’écrire en même temps sur le même "compte"
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from TicketAccount a where a.student.id = :studentId")
    Optional<TicketAccount> lockByStudentId(@Param("studentId") Long studentId);
}
