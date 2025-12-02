package com.backend.tutor_app.dto.tickets;

import com.backend.tutor_app.model.Student;
import com.backend.tutor_app.model.TicketAccount;
import com.backend.tutor_app.model.TicketTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO d'exposition d'un compte de tickets (crédits) pour un étudiant.
 * - Inclut le solde courant, la date de dernière recharge et les dernières transactions.
 * - Fournit des helpers de mapping fromEntity()/toEntity() pour cohérence avec l'architecture du projet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAccountDTO {

    /** UUID métier stable pour intégrations et référencement externe */
    private UUID uuid;

    /** Identifiant interne de l'étudiant propriétaire du compte */
    private Long studentId;

    /** Solde de tickets actuel */
    private Integer balance;

    /** Date de création du compte */
    private LocalDateTime createdAt;

    /** Date de dernière mise à jour du compte */
    private LocalDateTime updatedAt;

    /** Date de la dernière recharge (si connue) */
    private LocalDateTime lastRechargeAt;

    /** Dernières transactions à afficher (ex: Top 5) */
    private List<TicketTransactionDTO> lastTransactions;

    /**
     * Construit un TicketAccountDTO à partir d'une entité TicketAccount.
     */
    public static TicketAccountDTO fromEntity(TicketAccount account) {
        if (account == null) return null;
        return TicketAccountDTO.builder()
                .uuid(account.getUuid())
                .studentId(account.getStudent() != null ? account.getStudent().getId() : null)
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getLastUpdate())
                .build();
    }

    /**
     * Variante fromEntity permettant d'injecter la dernière recharge et une liste de transactions.
     */
    public static TicketAccountDTO fromEntity(TicketAccount account,
                                             LocalDateTime lastRechargeAt,
                                             List<TicketTransaction> lastTx) {
        TicketAccountDTO dto = fromEntity(account);
        if (dto == null) return null;
        dto.setLastRechargeAt(lastRechargeAt);
        if (lastTx != null) {
            dto.setLastTransactions(lastTx.stream()
                    .map(TicketTransactionDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * Construit une entité TicketAccount à partir d'un DTO.
     * ATTENTION:
     * - Cette méthode crée une entité basique; l'association Student doit être gérée par le service
     *   (ici on crée un proxy minimal si studentId est fourni).
     * - L'ID JPA (Long) n'est pas défini ici; il est géré par la base.
     */
    public static TicketAccount toEntity(TicketAccountDTO dto) {
        if (dto == null) return null;
        TicketAccount entity = new TicketAccount();
        entity.setUuid(dto.getUuid());
        entity.setBalance(dto.getBalance() != null ? dto.getBalance() : 0);
        // Association Student (proxy par id si fourni)
        if (dto.getStudentId() != null) {
            Student s = new Student();
            s.setId(dto.getStudentId());
            entity.setStudent(s);
        }
        // Dates gérées par l'audit; on peut recopier si nécessaire
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setLastUpdate(dto.getUpdatedAt());
        return entity;
    }
}
