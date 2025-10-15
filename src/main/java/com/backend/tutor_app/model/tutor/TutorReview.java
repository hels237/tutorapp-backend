package com.backend.tutor_app.model.tutor;

import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Lesson;
import com.backend.tutor_app.model.Student;
import com.backend.tutor_app.model.Tutor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TutorReview extends AbstractEntiity {


    @Column(nullable = false)
    private Integer rating; // 1-5 étoiles

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false; // Avis vérifié (étudiant a vraiment eu cours)

    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0; // Nombre de "utile"


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "lesson_id")
//    private Lesson lesson; // Cours associé à l'avis


}
