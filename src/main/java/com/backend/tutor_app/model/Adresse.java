package com.backend.tutor_app.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Embeddable
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Adresse {
    private String street ;

    private String houseNumber ;

    private Integer postalCode ;

    private String city ;

    private String country ;
}
