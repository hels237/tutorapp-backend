package com.backend.tutor_app.dto.tickets;

import com.backend.tutor_app.model.TicketAccount;
import com.backend.tutor_app.model.TicketRecharge;
import com.backend.tutor_app.model.TicketTransaction;
import com.backend.tutor_app.model.enums.TicketTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO d'exposition d'un mouvement de solde (transaction) du compte tickets.
 * - Sert à afficher l'historique côté API/UI (type, description, dates, soldes avant/après).
 * - Mappe les références utiles (accountUuid, rechargeUuid) pour faciliter le suivi.
 * - Fournit des helpers fromEntity()/toEntity() (sans MapStruct) pour cohérence projet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTransactionDTO {

    /** UUID métier de la transaction */
    private UUID uuid;

    /** Type de transaction: DEBIT ou CREDIT */
    private String type;

    /** Description fonctionnelle (ex: "Débit séance #123") */
    private String description;

    /** Identifiant de séance associé (si DEBIT lié à un cours) */
    private Long lessonId;

    /** Solde avant et après transaction */
    private Integer balanceBefore;
    private Integer balanceAfter;

    /** Date de création de la transaction */
    private LocalDateTime createdAt;

    /** Références pratiques (non obligatoires) */
    private UUID accountUuid;
    private UUID rechargeUuid;

    // ===================== Mappers =====================

    /**
     * Construit un DTO à partir d'une entité JPA TicketTransaction.
     */
    public static TicketTransactionDTO fromEntity(TicketTransaction entity) {
        if (entity == null) return null;
        return TicketTransactionDTO.builder()
                .uuid(entity.getUuid())
                .type(entity.getType() != null ? entity.getType().name() : null)
                .description(entity.getDescription())
                .lessonId(entity.getLessonId())
                .balanceBefore(entity.getBalanceBefore())
                .balanceAfter(entity.getBalanceAfter())
                .createdAt(entity.getCreatedAt())
                .accountUuid(entity.getAccount() != null ? entity.getAccount().getUuid() : null)
                .rechargeUuid(entity.getRecharge() != null ? entity.getRecharge().getUuid() : null)
                .build();
    }

    /**
     * Construit une entité TicketTransaction à partir d'un DTO.
     * ATTENTION:
     * - Les champs balanceBefore/balanceAfter doivent être fixés par le service métier (calcul du solde).
     * - Les relations (account, recharge) doivent être passées explicitement au mapper.
     */
    public static TicketTransaction toEntity(TicketTransactionDTO dto,
                                             TicketAccount account,
                                             TicketRecharge recharge) {
        if (dto == null) return null;
        TicketTransaction entity = new TicketTransaction();
        entity.setUuid(dto.getUuid());
        entity.setAccount(account);
        entity.setRecharge(recharge);
        if (dto.getType() != null) {
            try { entity.setType(TicketTransactionType.valueOf(dto.getType().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        entity.setDescription(dto.getDescription());
        entity.setLessonId(dto.getLessonId());
        entity.setBalanceBefore(dto.getBalanceBefore());
        entity.setBalanceAfter(dto.getBalanceAfter());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }
}
