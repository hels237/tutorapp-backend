package com.backend.tutor_app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Objet de statistiques métier non persisté pour un étudiant.
 * Utilisé par `Student` via un champ @Transient.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentStats {
    private Integer totalLessons;           // Nombre total de leçons
    private Integer completedLessons;       // Leçons terminées
    private Integer upcomingLessons;        // Leçons à venir
    private Integer canceledLessons;        // Leçons annulées

    private Double averageRating;           // Note moyenne reçue
    private Long totalMinutesStudied;       // Temps total étudié (en minutes)

    private Integer favoriteTutorsCount;    // Nombre de tuteurs favoris
    private Integer distinctSubjectsCount;  // Nombre de matières étudiées

    private LocalDateTime lastLessonAt;     // Date de la dernière leçon
    private LocalDateTime nextLessonAt;     // Date de la prochaine leçon
}
