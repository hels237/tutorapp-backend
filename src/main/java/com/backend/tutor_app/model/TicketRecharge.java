package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.TicketRechargeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente une opération d'achat/recharge de tickets.
 * - Créée lors de l'initiation de paiement (statut PENDING).
 * - Mise à jour par le webhook du prestataire de paiement (SUCCESS/FAILED/REFUNDED).
 * - Sert de source pour créditer le compte via une {@link TicketTransaction} de type CREDIT.
 * - Garantit l'idempotence grâce à {@code externalTransactionId} (unique si présent).
 * - Conserve le payload brut du webhook dans {@code rawPayload} pour audit et débogage.
 * Utilisation:
 * - Service: TicketRechargeService (initier, confirmer, réconcilier les paiements).
 * - Controller webhook: PaymentWebhookController (validation signature + mise à jour statut).
 * - Jamais utilisée pour débiter directement: le solde est modifié uniquement via TicketTransaction.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_recharges")
public class TicketRecharge extends AbstractEntiity {

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketRechargeStatus status;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "tickets_credited", nullable = false)
    private Integer ticketsCredited;

    @Column(name = "external_transaction_id", unique = true, length = 150)
    private String externalTransactionId;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;
}
