package com.backend.tutor_app.dto.user;

import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;

import java.time.LocalDateTime;

public class UserProfileDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePicture;
    private Role role;
    private UserStatus status;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private StudentProfileDto studentProfile;
    private TutorProfileDto tutorProfile;
    private ParentProfileDto parentProfile;
    private AdminProfileDto adminProfile;
}
