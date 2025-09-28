package com.backend.tutor_app.model.tutor;

import com.backend.tutor_app.model.AbstractEntiity;
import com.backend.tutor_app.model.Tutor;
import com.backend.tutor_app.model.enums.LanguageLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TutorLanguage extends AbstractEntiity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(name = "language_code", nullable = false)
    private String languageCode; // "fr", "en", "es", "de", etc.

    @Column(name = "language_name", nullable = false)
    private String languageName; // "Français", "English", "Español"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LanguageLevel level;

    @Column(name = "is_native", nullable = false)
    private Boolean isNative = false;


}
