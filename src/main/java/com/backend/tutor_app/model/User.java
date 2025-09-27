package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@SuperBuilder
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_user")
@Entity
public class User extends AbstractEntiity{

    private String firstName;

    private String lastName;

    private String password;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    private String photo;

    private Instant dateDeNaissance;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String avatar;

    private String phone;
}
