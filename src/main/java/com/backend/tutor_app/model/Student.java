package com.backend.tutor_app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

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


}
