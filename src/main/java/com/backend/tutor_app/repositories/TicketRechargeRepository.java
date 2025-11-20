package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.TicketRecharge;
import com.backend.tutor_app.model.enums.TicketRechargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'historique des recharges (achats) de tickets.
 * - Sert à appliquer l'idempotence via externalTransactionId (anti-doublon paiement).
 * - Permet de retrouver la dernière recharge d'un étudiant (affichage du dernier crédit).
 * - Expose des requêtes pour la réconciliation des paiements en statut PENDING.
 */
@Repository
public interface TicketRechargeRepository extends JpaRepository<TicketRecharge, Long> {

    /**
     * Recherche une recharge par son UUID métier.
     * Use cases: exposition REST, suivi technique, intégrations.
     */
    Optional<TicketRecharge> findByUuid(UUID uuid);

    /**
     * Recherche par identifiant de transaction du prestataire (idempotence).
     * Use cases: webhooks rejoués, doubles notifications; évite les crédits en double.
     */
    Optional<TicketRecharge> findByExternalTransactionId(String externalTransactionId);

    /**
     * Vérifie l'existence d'une recharge pour un externalTransactionId donné.
     * Use cases: garde-fou avant création/ajout de tickets.
     */
    boolean existsByExternalTransactionId(String externalTransactionId);

    /**
     * Dernière recharge enregistrée pour un étudiant (tri décroissant par date de création).
     * Use cases: afficher lastRechargeDate sur le compte.
     */
    Optional<TicketRecharge> findTopByStudent_IdOrderByCreatedAtDesc(Long studentId);

    /**
     * Liste des recharges en statut donné antérieures à un seuil temporel.
     * Use cases: job de réconciliation pour les PENDING trop anciens.
     */
    List<TicketRecharge> findByStatusAndCreatedAtBefore(TicketRechargeStatus status, LocalDateTime cutoff);

    /**
     * Variante explicitement ordonnée pour retrouver les recharges PENDING les plus anciennes.
     * Use cases: traiter en priorité les plus vieilles recharges non confirmées.
     */
    @Query("select r from TicketRecharge r where r.status = :status and r.createdAt < :cutoff order by r.createdAt asc")
    List<TicketRecharge> findPendingRechargesOlderThan(@Param("status") TicketRechargeStatus status,
                                                       @Param("cutoff") LocalDateTime cutoff);
}
