package com.backend.tutor_app.dto.tickets;

import com.backend.tutor_app.model.TicketAccount;
import com.backend.tutor_app.model.TicketTransaction;
import com.backend.tutor_app.model.enums.TicketTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Requête de débit d'une séance (consommation d'1 ticket = 1 séance de 45min).
 * - Protège contre le double débit via la référence de séance (lessonId) côté service/DB.
 * - Ne met PAS à jour le solde directement: le service construit une TicketTransaction et applique
 *   la mise à jour du solde de manière atomique.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitRequest {

    /** Identifiant de la séance/cours à débiter (référence d'idempotence) */
    @NotNull
    @Positive
    private Long lessonId;

    /** Description fonctionnelle optionnelle de l'opération */
    private String description;

    /**
     * Construit une entité TicketTransaction de type DEBIT à partir de la requête.
     * ATTENTION:
     * - balanceBefore/balanceAfter doivent être définis par le service après lecture/lock du compte.
     * - L'UUID métier et les timestamps peuvent être laissés à la responsabilité du service.
     */
    public TicketTransaction toTransactionEntity(TicketAccount account) {
        TicketTransaction tx = new TicketTransaction();
        tx.setAccount(account);
        tx.setType(TicketTransactionType.DEBIT);
        tx.setLessonId(this.lessonId);
        tx.setDescription(this.description != null ? this.description : "Débit séance #" + this.lessonId);
        tx.setCreatedAt(LocalDateTime.now());
        return tx;
    }
}
