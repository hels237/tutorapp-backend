package com.backend.tutor_app.model;


import com.backend.tutor_app.model.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@Entity
@DiscriminatorValue("TUTOR")
@NoArgsConstructor
@AllArgsConstructor
public class Tutor extends User{

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_lessons")
    private Integer totalLessons;


    //relation with Subject
    @ManyToMany
    @JoinTable(name = "tutor_subjects")
    private Set<Subject> subjects;

    //relation with TutorAvailability
    @OneToMany(mappedBy = "tutor")
    private List<TutorAvailability> availabilities;

    //relation with Qualification
    @OneToMany(mappedBy = "tutor")
    private List<Qualification> qualifications;





}
