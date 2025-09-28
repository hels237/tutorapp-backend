package com.backend.tutor_app.model;


import com.backend.tutor_app.model.enums.VerificationStatus;
import com.backend.tutor_app.model.tutor.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "La biographie ne peut pas dépasser 2000 caractères")
    private String bio;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Le tarif horaire doit être positif")
    private BigDecimal hourlyRate;

    @Column(name = "experience_years")
    @Min(value = 0, message = "L'expérience ne peut pas être négative")
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    @Min(value = 0)
    private Integer totalReviews = 0;

    @Column(name = "total_lessons")
    @Min(value = 0)
    private Integer totalLessons = 0;

    @Column(name = "response_time_hours")
    private Integer responseTimeHours = 24; // Temps de réponse moyen en heures

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "video_intro_url")
    private String videoIntroUrl;

    @Column(name = "timezone", length = 50)
    private String timezone = "Europe/Paris";

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;


    //relation with Subject
    @ManyToMany
    @JoinTable(
            name = "tutor_subjects"
            , joinColumns = @JoinColumn(name = "tutor_id")
            , inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> subjects;

    //relation with TutorAvailability
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TutorAvailability> availabilities = new ArrayList<>();

    //relation with Qualification
    //@Transient
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Qualification> qualifications = new ArrayList<>();

    //@Transient
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TutorSubject> tutorSubjects = new ArrayList<>();

    //@Transient
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

    public void updateRating(double newRating) {
        if (totalReviews == 0) {
            averageRating = newRating;
            totalReviews = 1;
        } else {
            double totalScore = averageRating * totalReviews;
            totalReviews++;
            averageRating = (totalScore + newRating) / totalReviews;
        }
    }

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

    //#########################################################################################


}
