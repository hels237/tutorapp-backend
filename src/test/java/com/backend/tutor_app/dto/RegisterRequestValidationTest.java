package com.backend.tutor_app.dto;

import com.backend.tutor_app.dto.Auth.RegisterRequest;
import com.backend.tutor_app.model.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour RegisterRequest - Validation des données
 * Teste toutes les validations Bean Validation du DTO
 */
@DisplayName("Tests RegisterRequest - Validation DTO")
class RegisterRequestValidationTest {

    private Validator validator;
    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .confirmPassword("SecurePass123!")
                .userType("student")
                .acceptTerms(true)
                .acceptMarketing(false)
                .build();
    }

    @Test
    @DisplayName("✅ Requête valide - Aucune violation")
    void testValidRequest_NoViolations() {
        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("❌ FirstName null - Violation")
    void testFirstName_Null() {
        // Given
        validRequest.setFirstName(null);

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("prénom");
    }

    @Test
    @DisplayName("❌ FirstName vide - Violation")
    void testFirstName_Empty() {
        // Given
        validRequest.setFirstName("");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("❌ FirstName trop court (1 caractère) - Violation")
    void testFirstName_TooShort() {
        // Given
        validRequest.setFirstName("J");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("entre 2 et 50");
    }

    @Test
    @DisplayName("❌ FirstName trop long (51 caractères) - Violation")
    void testFirstName_TooLong() {
        // Given
        validRequest.setFirstName("A".repeat(51));

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("❌ LastName null - Violation")
    void testLastName_Null() {
        // Given
        validRequest.setLastName(null);

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("❌ Email null - Violation")
    void testEmail_Null() {
        // Given
        validRequest.setEmail(null);

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("❌ Email format invalide - Violation")
    void testEmail_InvalidFormat() {
        // Given
        validRequest.setEmail("invalid-email");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .containsIgnoringCase("email");
    }

    @Test
    @DisplayName("✅ Email valide - Pas de violation")
    void testEmail_Valid() {
        // Given
        validRequest.setEmail("test@example.com");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("❌ Password null - Violation")
    void testPassword_Null() {
        // Given
        validRequest.setPassword(null);

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("❌ Password trop court (7 caractères) - Violation")
    void testPassword_TooShort() {
        // Given
        validRequest.setPassword("Short1!");
        validRequest.setConfirmPassword("Short1!");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("au moins 8");
    }

    @Test
    @DisplayName("❌ Password sans majuscule - Violation")
    void testPassword_NoUppercase() {
        // Given
        validRequest.setPassword("securepass123!");
        validRequest.setConfirmPassword("securepass123!");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .containsIgnoringCase("majuscule");
    }

    @Test
    @DisplayName("❌ Password sans minuscule - Violation")
    void testPassword_NoLowercase() {
        // Given
        validRequest.setPassword("SECUREPASS123!");
        validRequest.setConfirmPassword("SECUREPASS123!");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .containsIgnoringCase("minuscule");
    }

    @Test
    @DisplayName("❌ Password sans chiffre - Violation")
    void testPassword_NoDigit() {
        // Given
        validRequest.setPassword("SecurePass!");
        validRequest.setConfirmPassword("SecurePass!");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .containsIgnoringCase("chiffre");
    }

    @Test
    @DisplayName("❌ Password sans caractère spécial - Violation")
    void testPassword_NoSpecialChar() {
        // Given
        validRequest.setPassword("SecurePass123");
        validRequest.setConfirmPassword("SecurePass123");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .containsIgnoringCase("spécial");
    }

    @Test
    @DisplayName("❌ ConfirmPassword null - Violation")
    void testConfirmPassword_Null() {
        // Given
        validRequest.setConfirmPassword(null);

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("❌ Password et ConfirmPassword différents - Violation")
    void testPassword_Mismatch() {
        // Given
        validRequest.setPassword("SecurePass123!");
        validRequest.setConfirmPassword("DifferentPass456!");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .containsIgnoringCase("correspondent pas");
    }

    @Test
    @DisplayName("❌ UserType null - Violation")
    void testUserType_Null() {
        // Given
        validRequest.setUserType(null);

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("❌ UserType vide - Violation")
    void testUserType_Empty() {
        // Given
        validRequest.setUserType("");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("✅ UserType 'student' - Pas de violation")
    void testUserType_Student() {
        // Given
        validRequest.setUserType("student");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("✅ UserType 'tutor' - Pas de violation")
    void testUserType_Tutor() {
        // Given
        validRequest.setUserType("tutor");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("✅ UserType 'parent' - Pas de violation")
    void testUserType_Parent() {
        // Given
        validRequest.setUserType("parent");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("✅ Conversion UserType 'student' -> Role.STUDENT")
    void testGetUserTypeAsRole_Student() {
        // Given
        validRequest.setUserType("student");

        // When
        Role role = validRequest.getUserTypeAsRole();

        // Then
        assertThat(role).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("✅ Conversion UserType 'STUDENT' (majuscule) -> Role.STUDENT")
    void testGetUserTypeAsRole_StudentUppercase() {
        // Given
        validRequest.setUserType("STUDENT");

        // When
        Role role = validRequest.getUserTypeAsRole();

        // Then
        assertThat(role).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("✅ Conversion UserType 'tutor' -> Role.TUTOR")
    void testGetUserTypeAsRole_Tutor() {
        // Given
        validRequest.setUserType("tutor");

        // When
        Role role = validRequest.getUserTypeAsRole();

        // Then
        assertThat(role).isEqualTo(Role.TUTOR);
    }

    @Test
    @DisplayName("✅ Conversion UserType 'parent' -> Role.PARENT")
    void testGetUserTypeAsRole_Parent() {
        // Given
        validRequest.setUserType("parent");

        // When
        Role role = validRequest.getUserTypeAsRole();

        // Then
        assertThat(role).isEqualTo(Role.PARENT);
    }

    @Test
    @DisplayName("✅ Conversion UserType invalide -> Role.STUDENT (défaut)")
    void testGetUserTypeAsRole_InvalidDefaultsToStudent() {
        // Given
        validRequest.setUserType("invalid_type");

        // When
        Role role = validRequest.getUserTypeAsRole();

        // Then
        assertThat(role).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("✅ Conversion UserType null -> Role.STUDENT (défaut)")
    void testGetUserTypeAsRole_NullDefaultsToStudent() {
        // Given
        validRequest.setUserType(null);

        // When
        Role role = validRequest.getUserTypeAsRole();

        // Then
        assertThat(role).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("✅ PhoneNumber valide - Pas de violation")
    void testPhoneNumber_Valid() {
        // Given
        validRequest.setPhoneNumber("+33612345678");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("❌ PhoneNumber format invalide - Violation")
    void testPhoneNumber_InvalidFormat() {
        // Given
        validRequest.setPhoneNumber("invalid-phone");

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);

        // Then
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("✅ Méthodes utilitaires isStudent/isTutor/isParent")
    void testUtilityMethods() {
        // Test isStudent
        validRequest.setUserType("student");
        assertThat(validRequest.isStudent()).isTrue();
        assertThat(validRequest.isTutor()).isFalse();
        assertThat(validRequest.isParent()).isFalse();

        // Test isTutor
        validRequest.setUserType("tutor");
        assertThat(validRequest.isStudent()).isFalse();
        assertThat(validRequest.isTutor()).isTrue();
        assertThat(validRequest.isParent()).isFalse();

        // Test isParent
        validRequest.setUserType("parent");
        assertThat(validRequest.isStudent()).isFalse();
        assertThat(validRequest.isTutor()).isFalse();
        assertThat(validRequest.isParent()).isTrue();
    }
}
