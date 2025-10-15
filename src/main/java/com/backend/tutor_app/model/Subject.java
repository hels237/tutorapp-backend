package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.SubjectCategory;
import com.backend.tutor_app.model.tutor.TutorSubject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Subject extends AbstractEntiity{

    @Column(nullable = false, unique = true)
    private String code; // "mathematics", "physics", "chemistry", etc.

    @Column(nullable = false)
    private String name; // "Mathématiques", "Physique", "Chimie"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_name")
    private String iconName; // Pour l'affichage frontend

    //@Column(name = "color_code")
    //private String colorCode; // Code couleur pour l'UI

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectCategory category; // SCIENCES, LANGUAGES, HUMANITIES, etc.

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Relations
    @ManyToMany(mappedBy = "subjects")
    private Set<Tutor> tutors;

//    @OneToMany(mappedBy = "subject")
//    private List<Lesson> lessons;

    @OneToMany(mappedBy = "subject")
    private List<TutorSubject> tutorSubjects; // Table de liaison avec tarifs

}
