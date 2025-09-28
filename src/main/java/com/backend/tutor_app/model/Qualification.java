package com.backend.tutor_app.model;


import com.backend.tutor_app.model.enums.QualificationType;
import com.backend.tutor_app.model.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Qualification extends AbstractEntiity{

    @Column(nullable = false)
    private String title; // "Agrégation de Mathématiques", "Master en Physique"

    @Column(nullable = false)
    private String institution; // "École Normale Supérieure", "Université Paris-Saclay"

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "field_of_study")
    private String fieldOfStudy; // Domaine d'étude

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "document_url")
    private String documentUrl; // URL du document justificatif

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private String verifiedBy; // Admin qui a vérifié


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;


}
