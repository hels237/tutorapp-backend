package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.TicketAccount;
import com.backend.tutor_app.model.TicketTransaction;
import com.backend.tutor_app.model.enums.TicketTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour l'historique des mouvements de solde (transactions) du compte tickets.
 * - Permet de récupérer rapidement les dernières opérations (Top 5) pour l'affichage tableau de bord.
 * - Fournit des recherches par UUID métier et par étudiant.
 * - Aide à prévenir le double débit d'une même séance via des méthodes de lookup sur (type, lessonId).
 */
@Repository
public interface TicketTransactionRepository extends JpaRepository<TicketTransaction, Long> {

    /**
     * Recherche une transaction par son UUID métier.
     * Use cases: exposition REST, vérification ciblée, audit technique.
     */
    Optional<TicketTransaction> findByUuid(UUID uuid);

    /**
     * Retourne les 5 dernières transactions d'un compte (ordre décroissant sur createdAt).
     * Use cases: affichage rapide sur le tableau de bord de l'étudiant.
     */
    List<TicketTransaction> findTop5ByAccount_IdOrderByCreatedAtDesc(Long accountId);

    /**
     * Variante: accepte directement l'entité de compte.
     */
    List<TicketTransaction> findTop5ByAccountOrderByCreatedAtDesc(TicketAccount account);

    /**
     * Historique complet des transactions pour un étudiant, du plus récent au plus ancien.
     * Use cases: page dédiée "Historique" sans pagination côté dashboard.
     */
    List<TicketTransaction> findByAccount_Student_IdOrderByCreatedAtDesc(Long studentId);

    /**
     * Recherche une transaction par type et identifiant de séance.
     * Use cases: anti double-débit pour la même séance (type=DEBIT, même lessonId).
     */
    Optional<TicketTransaction> findByTypeAndLessonId(TicketTransactionType type, Long lessonId);

    /**
     * Vérifie l'existence d'une transaction pour (type, lessonId).
     * Use cases: garde-fou avant d'initier un nouveau débit pour la même séance.
     */
    boolean existsByTypeAndLessonId(TicketTransactionType type, Long lessonId);
}
