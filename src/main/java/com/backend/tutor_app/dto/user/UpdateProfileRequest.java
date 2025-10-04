package com.backend.tutor_app.dto.user;

import com.backend.tutor_app.dto.UpdateParentProfileRequest;
import com.backend.tutor_app.dto.UpdateStudentProfileRequest;
import com.backend.tutor_app.dto.UpdateTutorProfileRequest;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(min = 2, max = 50) private String firstName;
    @Size(min = 2, max = 50) private String lastName;
    private String phoneNumber;
    private String profilePicture;
    private UpdateStudentProfileRequest studentProfile; // optionnel selon role
    private UpdateTutorProfileRequest tutorProfile;     // optionnel selon role
    private UpdateParentProfileRequest parentProfile;   // optionnel selon role
}
