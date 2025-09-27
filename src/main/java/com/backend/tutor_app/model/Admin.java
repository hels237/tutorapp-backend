package com.backend.tutor_app.model;


import com.backend.tutor_app.model.enums.AdminLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("ADMIN")
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User{

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level")
    private AdminLevel adminLevel;



}
