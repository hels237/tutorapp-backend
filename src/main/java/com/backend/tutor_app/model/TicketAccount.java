package com.backend.tutor_app.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Compte de tickets (crédits) d'un étudiant.
 * - 1 seul compte par étudiant (contrainte unique sur student_id).
 * - Stocke le solde actuel (balance) exprimé en nombre de tickets.
 * - Créé automatiquement à l'inscription de l'étudiant (ou au premier accès).
 * - N'est jamais modifié directement: toute modification passe par TicketTransaction (DEBIT/CREDIT).
 * - Les opérations de débit/crédit sont réalisées de manière atomique (verrouillage sur le compte).
 * Utilisation:
 * - Services: TicketAccountService (credit/debit), TicketRechargeService (crédit via webhook).
 * - API: TicketAccountController pour la consultation du solde.
 * - Audit & Traçabilité: l'historique est conservé dans TicketTransaction.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_accounts")
public class TicketAccount extends AbstractEntiity {

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private Integer balance = 0;
}
