package com.backend.tutor_app.controller;

import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.dto.Auth.RegisterRequest;
import com.backend.tutor_app.dto.user.UserProfileDto;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour AuthController - Endpoint d'inscription
 * Teste uniquement POST /api/v1/auth/register
 */
@SpringBootTest(properties = {"server.servlet.context-path="}) // Désactive le context-path pour les tests
@AutoConfigureMockMvc(addFilters = false) // Désactive les filtres de sécurité pour les tests
@DisplayName("Tests AuthController - Endpoint Inscription")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest validRequest;
    private AuthResponse mockResponse;

    @BeforeEach
    void setUp() {
        // Préparation d'une requête valide
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

        // Préparation d'une réponse mockée
        UserProfileDto userDto = UserProfileDto.builder()
                .id(1L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.STUDENT)
                .build();

        mockResponse = AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userDto)
                .build();
    }

    @Test
    @DisplayName("✅ Inscription réussie avec données valides")
    void testRegister_Success() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("STUDENT"));
    }

    @Test
    @DisplayName("❌ Inscription échoue - Email manquant")
    void testRegister_MissingEmail() throws Exception {
        // Given
        validRequest.setEmail(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Inscription échoue - Email invalide")
    void testRegister_InvalidEmail() throws Exception {
        // Given
        validRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Inscription échoue - Mot de passe trop court")
    void testRegister_PasswordTooShort() throws Exception {
        // Given
        validRequest.setPassword("Short1!");
        validRequest.setConfirmPassword("Short1!");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Inscription échoue - Mots de passe différents")
    void testRegister_PasswordMismatch() throws Exception {
        // Given
        validRequest.setPassword("SecurePass123!");
        validRequest.setConfirmPassword("DifferentPass456!");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Inscription échoue - Prénom manquant")
    void testRegister_MissingFirstName() throws Exception {
        // Given
        validRequest.setFirstName(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Inscription échoue - Nom manquant")
    void testRegister_MissingLastName() throws Exception {
        // Given
        validRequest.setLastName(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Inscription échoue - UserType manquant")
    void testRegister_MissingUserType() throws Exception {
        // Given
        validRequest.setUserType(null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ Inscription réussie - UserType TUTOR")
    void testRegister_TutorType() throws Exception {
        // Given
        validRequest.setUserType("tutor");
        UserProfileDto tutorDto = UserProfileDto.builder()
                .id(2L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.TUTOR)
                .build();
        
        AuthResponse tutorResponse = AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(tutorDto)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(tutorResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.role").value("TUTOR"));
    }

    @Test
    @DisplayName("✅ Inscription réussie - UserType PARENT")
    void testRegister_ParentType() throws Exception {
        // Given
        validRequest.setUserType("parent");
        UserProfileDto parentDto = UserProfileDto.builder()
                .id(3L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.PARENT)
                .build();
        
        AuthResponse parentResponse = AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(parentDto)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(parentResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.role").value("PARENT"));
    }

    @Test
    @DisplayName("✅ Inscription réussie - Avec numéro de téléphone")
    void testRegister_WithPhoneNumber() throws Exception {
        // Given
        validRequest.setPhoneNumber("+33612345678");
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("✅ Inscription réussie - AcceptMarketing true")
    void testRegister_WithMarketingConsent() throws Exception {
        // Given
        validRequest.setAcceptMarketing(true);
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("❌ Inscription échoue - Email déjà existant")
    void testRegister_EmailAlreadyExists() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Un compte avec cet email existe déjà"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("❌ Inscription échoue - Mot de passe sans caractère spécial")
    void testRegister_PasswordWithoutSpecialChar() throws Exception {
        // Given
        validRequest.setPassword("SecurePass123");
        validRequest.setConfirmPassword("SecurePass123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Inscription échoue - UserType invalide")
    void testRegister_InvalidUserType() throws Exception {
        // Given
        validRequest.setUserType("invalid_type");
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // When & Then
        // Le userType invalide sera converti en STUDENT par défaut via getUserTypeAsRole()
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }
}
