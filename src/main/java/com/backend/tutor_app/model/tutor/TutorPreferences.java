package com.backend.tutor_app.model.tutor;

import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Tutor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TutorPreferences extends AbstractEntiity {


    @Column(name = "auto_accept_bookings", nullable = false)
    private Boolean autoAcceptBookings = false;

    @Column(name = "advance_booking_hours", nullable = false)
    private Integer advanceBookingHours = 24; // Préavis minimum pour réservation

    @Column(name = "max_students_per_lesson", nullable = false)
    private Integer maxStudentsPerLesson = 1;

    @Column(name = "lesson_duration_minutes", nullable = false)
    private Integer lessonDurationMinutes = 60;

    @Column(name = "break_between_lessons", nullable = false)
    private Integer breakBetweenLessons = 15; // minutes

    @Column(name = "marketing_emails", nullable = false)
    private Boolean marketingEmails = false;

    @Column(nullable = false)
    private String timezone = "Europe/Paris";

    @Column(nullable = false)
    private String currency = "EUR";


    // Notifications
    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications = true;

    @Column(name = "sms_notifications", nullable = false)
    private Boolean smsNotifications = false;

    @Column(name = "push_notifications", nullable = false)
    private Boolean pushNotifications = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

}
