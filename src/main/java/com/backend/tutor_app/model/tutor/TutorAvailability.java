package com.backend.tutor_app.model.tutor;


import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Lesson;
import com.backend.tutor_app.model.Tutor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TutorAvailability extends AbstractEntiity {

    @Column(name = "max_students")
    private Integer maxStudents; // Nombre max d'étudiants pour ce créneau

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = true; // Récurrent chaque semaine

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ElementCollection
    @CollectionTable(name = "availability_subjects")
    private Set<String> subjectCodes; // Matières disponibles pour ce créneau

    @Column(name = "timezone")
    private String timezone; // Fuseau horaire du tuteur


    // Relations
    @OneToMany(mappedBy = "availability")
    private List<Lesson> lessons; // Cours réservés sur ce créneau

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;
}
