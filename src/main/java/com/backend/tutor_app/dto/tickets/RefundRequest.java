package com.backend.tutor_app.dto.tickets;

import com.backend.tutor_app.model.TicketAccount;
import com.backend.tutor_app.model.TicketTransaction;
import com.backend.tutor_app.model.enums.TicketTransactionType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Requête de remboursement d'un ticket consommé (crédit de retour).
 * - Cas d'usage: séance annulée ou non tenue → restituer 1 ticket sur le compte.
 * - Ne concerne PAS le remboursement d'un achat (recharge) qui, selon la spec, n'est pas remboursable.
 * - La référence peut être la transaction DEBIT d'origine (debitTransactionUuid) ou la même lessonId.
 * - Le service validera l'existence du débit d'origine et appliquera l'idempotence adéquate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    /** Référence de la transaction DEBIT d'origine (préférée pour précision) */
    private UUID debitTransactionUuid;

    /** Identifiant de la séance d'origine (alternative si l'UUID n'est pas connu) */
    private Long lessonId;

    /** Raison/justification visible côté audit et support */
    @Size(max = 500)
    private String reason;

    /**
     * Indique si la requête contient au moins une référence exploitable.
     * À vérifier côté service (et lever une ValidationException si false).
     */
    public boolean hasReference() {
        return debitTransactionUuid != null || (lessonId != null && lessonId > 0);
    }

    /**
     * Construit une entité TicketTransaction de type CREDIT (remboursement d'un débit séance).
     * ATTENTION:
     * - balanceBefore/balanceAfter sont fixés par le service métier après lecture/lock du compte.
     * - Lien avec la séance est recopié via lessonId pour traçabilité.
     */
    public TicketTransaction toCreditTransactionEntity(TicketAccount account) {
        TicketTransaction tx = new TicketTransaction();
        tx.setAccount(account);
        tx.setType(TicketTransactionType.CREDIT);
        tx.setLessonId(this.lessonId);
        String desc = (this.reason != null && !this.reason.isBlank())
                ? "Remboursement séance (" + this.reason + ")"
                : "Remboursement séance";
        tx.setDescription(desc);
        tx.setCreatedAt(LocalDateTime.now());
        return tx;
    }
}
