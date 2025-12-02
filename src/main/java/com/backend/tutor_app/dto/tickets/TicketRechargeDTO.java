package com.backend.tutor_app.dto.tickets;

import com.backend.tutor_app.model.Student;
import com.backend.tutor_app.model.TicketRecharge;
import com.backend.tutor_app.model.enums.TicketRechargeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO d'exposition d'une recharge (achat) de tickets.
 * - Contient le statut, les montants, la devise et les métadonnées utiles côté API.
 * - Fournit des helpers fromEntity()/toEntity() pour rester cohérent avec l'architecture du projet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRechargeDTO {

    /** UUID métier stable pour suivi et intégrations */
    private UUID uuid;

    /** Identifiant interne de l'étudiant */
    private Long studentId;

    /** Timestamps */
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    /** Statut (PENDING, SUCCESS, FAILED, REFUNDED) exposé en String pour simplicité API */
    private String status;

    /** Montant de la transaction et devise ISO 4217 */
    private BigDecimal amount;
    private String currency;

    /** Nombre de tickets crédités (déterminé au SUCCESS) */
    private Integer ticketsCredited;

    /** Identifiant transactionnel du prestataire (idempotence et debug) */
    private String externalTransactionId;

    /** Moyen de paiement (ex: CARD, PAYPAL, MOMO) et commentaire optionnel */
    private String paymentMethod;
    private String comment;

    // ===================== Mappers =====================

    /**
     * Construit un DTO à partir d'une entité JPA.
     */
    public static TicketRechargeDTO fromEntity(TicketRecharge entity) {
        if (entity == null) return null;
        return TicketRechargeDTO.builder()
                .uuid(entity.getUuid())
                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .createdAt(entity.getCreatedAt())
                .paidAt(entity.getPaidAt())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .ticketsCredited(entity.getTicketsCredited())
                .externalTransactionId(entity.getExternalTransactionId())
                .paymentMethod(entity.getPaymentMethod())
                .comment(entity.getComment())
                .build();
    }

    /**
     * Construit une entité JPA à partir du DTO.
     * ATTENTION: l'association Student doit être fournie (proxy minimal accepté).
     */
    public static TicketRecharge toEntity(TicketRechargeDTO dto, Student student) {
        if (dto == null) return null;
        TicketRecharge entity = new TicketRecharge();
        entity.setUuid(dto.getUuid());
        entity.setStudent(student);
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setPaidAt(dto.getPaidAt());
        if (dto.getStatus() != null) {
            try { entity.setStatus(TicketRechargeStatus.valueOf(dto.getStatus().toUpperCase())); } catch (IllegalArgumentException ignored) {}
        }
        entity.setAmount(dto.getAmount());
        entity.setCurrency(dto.getCurrency());
        entity.setTicketsCredited(dto.getTicketsCredited() != null ? dto.getTicketsCredited() : 0);
        entity.setExternalTransactionId(dto.getExternalTransactionId());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setComment(dto.getComment());
        return entity;
    }
}
