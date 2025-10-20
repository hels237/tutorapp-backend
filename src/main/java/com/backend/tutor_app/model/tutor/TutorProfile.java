package com.backend.tutor_app.model.tutor;

import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Utilisateur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter @Setter
@SuperBuilder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TutorProfile extends AbstractEntiity {

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String experience; // "5 ans"

    private String responseTime; // "< 1h"

    @ElementCollection
    @CollectionTable(name = "tutor_levels")
    private Set<String> levels;

    @ElementCollection
    @CollectionTable(name = "tutor_languages")
    private Set<String> languages;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Utilisateur utilisateur;


    private boolean isVerified = false;
    private boolean isAvailable = true;
}
