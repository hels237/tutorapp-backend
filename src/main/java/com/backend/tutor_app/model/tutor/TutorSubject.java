package com.backend.tutor_app.model.tutor;

import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Subject;
import com.backend.tutor_app.model.Tutor;
import com.backend.tutor_app.model.enums.EducationLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Set;

@Setter @Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TutorSubject extends AbstractEntiity {

    @Column(name = "hourly_rate", nullable = false)
    private BigDecimal hourlyRate; // Tarif spécifique par matière

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tutor_subject_levels")
    private Set<EducationLevel> levels; // Niveaux enseignés pour cette matière

    @Column(name = "years_experience")
    private Integer yearsExperience; // Expérience spécifique dans cette matière

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false; // Matière principale du tuteur



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
