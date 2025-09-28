package com.backend.tutor_app.dto.user;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateProfileRequestDto {

    @Size(min = 2, max = 50)
    private String firstName;

    @Size(min = 2, max = 50)
    private String lastName;

    private String phoneNumber;
    private String profilePicture;

    private String bio; // Tutor
    private BigDecimal hourlyRate; // Tutor
    private String schoolLevel; // Student
}
