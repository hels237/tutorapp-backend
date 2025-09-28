package com.backend.tutor_app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("PARENT")
@NoArgsConstructor
@AllArgsConstructor
public class Parent extends User{

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "relationship_to_children", length = 50)
    private String relationshipToChildren; // "parent", "guardian", etc.

    @Column(name = "preferred_communication", length = 20)
    private String preferredCommunication = "email"; // "email", "sms", "phone"


    // relation with Student
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Student> student = new ArrayList<>();


    // Méthodes utilitaires
    public void addChild(Student child) {
       student.add(child);
        child.setParent(this);
    }

    public void removeChild(Student child) {
        student.remove(child);
        child.setParent(null);
    }

}
