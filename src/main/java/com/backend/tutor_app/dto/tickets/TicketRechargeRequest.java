package com.backend.tutor_app.dto.tickets;

import com.backend.tutor_app.model.Student;
import com.backend.tutor_app.model.TicketRecharge;
import com.backend.tutor_app.model.enums.TicketRechargeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Requête d'initiation d'une recharge (achat) de tickets.
 * - Fourni par l'API pour démarrer un paiement auprès du prestataire.
 * - Ne crédite pas immédiatement le compte: l'ajout effectif des tickets interviendra au webhook SUCCESS.
 * - Utilise un mapping toEntity(Student) pour préparer un enregistrement PENDING.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRechargeRequest {

    /** Montant à débiter (dans la devise donnée) */
    @NotNull
    @Positive
    private BigDecimal amount;

    /** Devise au format ISO 4217, ex: EUR, USD, XOF */
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Devise invalide (ISO 4217 attendu, ex: EUR)")
    private String currency;

    /** Optionnel: mode de paiement prévu (carte, paypal, mobile money, etc.) */
    private String paymentMethod;

    /** Optionnel: commentaire côté utilisateur (sera stocké sur la recharge) */
    private String comment;

    /**
     * Construit une entité TicketRecharge en statut PENDING à partir de la requête.
     * ATTENTION: le calcul du nombre de tickets crédités sera déterminé par la politique tarifaire
     * au moment de la confirmation (webhook) et non ici.
     */
    public TicketRecharge toEntity(Student student) {
        TicketRecharge recharge = new TicketRecharge();
        recharge.setStudent(student);
        recharge.setAmount(this.amount);
        recharge.setCurrency(this.currency);
        recharge.setPaymentMethod(this.paymentMethod);
        recharge.setComment(this.comment);
        recharge.setStatus(TicketRechargeStatus.PENDING);
        recharge.setPaidAt(null);
        recharge.setExternalTransactionId(null); // sera défini par le gateway ou au webhook
        recharge.setRawPayload(null);
        recharge.setTicketsCredited(0); // par défaut, sera fixé au SUCCESS
        recharge.setCreatedAt(LocalDateTime.now());
        return recharge;
    }
}
