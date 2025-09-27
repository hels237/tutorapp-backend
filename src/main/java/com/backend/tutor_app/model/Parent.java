package com.backend.tutor_app.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("PARENT")
@NoArgsConstructor
@AllArgsConstructor
public class Parent extends User{

    // relation with Student
    @OneToMany(mappedBy = "parent")
    private List<Student> student;

}
