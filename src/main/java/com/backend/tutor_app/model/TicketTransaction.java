package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.TicketTransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Mouvement de solde associé à un compte de tickets.
 * - Créé pour chaque opération métier de crédit (CREDIT) ou de débit (DEBIT).
 * - Pour un DEBIT lié à une séance, {@code lessonId} est renseigné; une contrainte d'unicité empêche
 *   un double débit de la même séance (voir migration V8).
 * - Pour un CREDIT issu d'une recharge, {@link TicketRecharge} est référencée dans {@code recharge}.
 * - Contient un UUID business, le solde avant/après et une description pour l'audit.
 * Utilisation:
 * - Écrit uniquement par les services (ex. TicketAccountService pour débit/crédit,
 *   TicketRechargeService pour crédit via webhook).
 * - Consulté par un contrôleur pour l'historique des transactions.
 * - Ne modifie jamais le solde par lui‑même: la mise à jour du solde se fait atomiquement sur
 *   {@link TicketAccount}, cette entité sert de journal immuable.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_transactions")
public class TicketTransaction extends AbstractEntiity {

    @Column(name = "uuid", nullable = false, unique = true)
    private java.util.UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private TicketAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recharge_id")
    private TicketRecharge recharge;

    @Column(name = "lesson_id")
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TicketTransactionType type; // DEBIT or CREDIT

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "balance_before", nullable = false)
    private Integer balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;
}
