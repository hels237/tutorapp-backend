package com.backend.tutor_app.model;


import com.backend.tutor_app.model.enums.VerificationStatus;
import com.backend.tutor_app.model.tutor.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter
@Entity
@DiscriminatorValue("TUTOR")
@NoArgsConstructor
@AllArgsConstructor
public class Tutor extends User{

    @Column(columnDefinition = "TEXT",name = "biography")
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

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "total_lessons")
    private Integer totalLessons;

    @Column(name = "response_time_hours")
    private Integer responseTimeHours = 24; // Temps de réponse moyen en heures

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "video_intro_url")
    private String videoIntroUrl;

    @Column(name = "location")
    private String location;



    //relation with Subject
    @ManyToMany
    @JoinTable(name = "tutor_subjects")
    private Set<Subject> subjects;

    //relation with TutorAvailability
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TutorAvailability> availabilities = new ArrayList<>();

    //relation with Qualification
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Qualification> qualifications = new ArrayList<>();


    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TutorSubject> tutorSubjects = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TutorLanguage> languages = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TutorReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TutorDocument> documents = new ArrayList<>();

    @OneToOne(mappedBy = "tutor", cascade = CascadeType.ALL)
    private TutorPreferences preferences;

    @OneToMany(mappedBy = "tutor", fetch = FetchType.LAZY)
    private List<Lesson> lessons = new ArrayList<>();

    @ManyToMany(mappedBy = "favoriteTutors", fetch = FetchType.LAZY)
    @JoinTable(
            name="student_favorite_tutors",
            joinColumns = @JoinColumn(name="tutor_id"),
            inverseJoinColumns = @JoinColumn(name="student_id")
    )
    private Set<Student> favoriteByStudents = new HashSet<>();



    // Méthodes utilitaires
    public void updateAverageRating() {
        if (reviews.isEmpty()) {
            this.averageRating = 0.0;
            this.totalReviews = 0;
        } else {
            this.averageRating = reviews.stream()
                    .mapToInt(TutorReview::getRating)
                    .average()
                    .orElse(0.0);
            this.totalReviews = reviews.size();
        }
    }

    public List<Subject> getSubjects() {
        return tutorSubjects.stream()
                .map(TutorSubject::getSubject)
                .toList();
    }

    public BigDecimal getHourlyRateForSubject(String subjectCode) {
        return tutorSubjects.stream()
                .filter(ts -> ts.getSubject().getCode().equals(subjectCode))
                .findFirst()
                .map(TutorSubject::getHourlyRate)
                .orElse(this.hourlyRate);
    }

}
