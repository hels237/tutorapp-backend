package com.backend.tutor_app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter @Getter
@Entity
@SuperBuilder
@DiscriminatorValue("STUDENT")
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User{

    @Column(name = "school_level", length = 50)
    private String schoolLevel; // "Terminale S", "L1", etc.

    @Column(name = "school_name", length = 100)
    private String schoolName; // "Lycée Victor Hugo"

    @Column(name = "birth_date")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate birthDate;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "learning_style", length = 20)
    private String learningStyle; // "visual", "auditory", "kinesthetic"

    @Column(name = "goals", columnDefinition = "TEXT")
    private String goals;

    // relation with Lesson
//    @OneToMany(mappedBy = "student")
//    private List<Lesson> lessons;

    // relation with Tutor for favorite tutors
    @ManyToMany
    @JoinTable(
            name = "student_favorite_tutors",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "tutor_id")
    )
    private Set<Tutor> favoriteTutors;

    @ElementCollection
    @CollectionTable(name = "student_preferred_subjects",
            joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "subject_code")
    private Set<String> preferredSubjects = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "student_availability",
            joinColumns = @JoinColumn(name = "student_id")
    )
    @Column(name = "time_slot")
    private Set<String> availability = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    // Statistiques calculées
    @Transient
    private StudentStats stats;




}
