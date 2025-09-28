package com.backend.tutor_app.dto.user;

import com.backend.tutor_app.dto.UpdateParentProfileRequest;
import com.backend.tutor_app.dto.UpdateStudentProfileRequest;
import com.backend.tutor_app.dto.UpdateTutorProfileRequest;
import jakarta.validation.constraints.Size;

public class UpdateUserProfileRequest {
    @Size(min = 2, max = 50) private String firstName;
    @Size(min = 2, max = 50) private String lastName;
    private String phoneNumber;
    private String profilePicture;

    // #------------------ optionnel selon role
    private UpdateStudentProfileRequest studentProfile;
    private UpdateTutorProfileRequest tutorProfile;     // optionnel selon role
    private UpdateParentProfileRequest parentProfile;   // optionnel selon role
}
