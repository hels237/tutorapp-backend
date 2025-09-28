package com.backend.tutor_app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Setter @Getter
@Entity
@SuperBuilder
@DiscriminatorValue("STUDENT")
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User{

    @Column(name = "school_level")
    private String schoolLevel;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    // relation with Parent
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;

    // relation with Lesson
    @OneToMany(mappedBy = "student")
    private List<Lesson> lessons;

    // relation with Tutor for favorite tutors
    @ManyToMany
    @JoinTable(
            name = "student_favorite_tutors",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "tutor_id")
    )
    private Set<Tutor> favoriteTutors;


}
