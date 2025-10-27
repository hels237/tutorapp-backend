package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.dto.Auth.RegisterRequest;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.Spy;

/**
 * Tests unitaires pour AuthServiceImpl - Logique métier d'inscription
 * Teste la logique de création d'utilisateur, validation et génération de tokens
 */
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//@DisplayName("Tests AuthServiceImpl - Logique Inscription")
class AuthServiceImplTest {
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private TokenService tokenService;
//
//    @Mock
//    private EmailService emailService;
//
//    @Mock
//    private RateLimitService rateLimitService;
//
//    @Spy
//    @InjectMocks
//    private AuthServiceImpl authService;
//
//    private RegisterRequest validRequest;
//    private Utilisateur mockUser;
//
//    @BeforeEach
//    void setUp() {
//        validRequest = RegisterRequest.builder()
//                .firstName("John")
//                .lastName("Doe")
//                .email("john.doe@example.com")
//                .password("SecurePass123!")
//                .confirmPassword("SecurePass123!")
//                .userType("student")
//                .acceptTerms(true)
//                .acceptMarketing(false)
//                .build();
//
//        mockUser = Utilisateur.builder()
//                .id(1L)
//                .email("john.doe@example.com")
//                .firstName("John")
//                .lastName("Doe")
//                .password("encoded-password")
//                .role(Role.STUDENT)
//                .status(UserStatus.PENDING_VERIFICATION)
//                .emailVerified(false)
//                .loginAttempts(0)
//                .acceptTerms(true)
//                .acceptMarketing(false)
//                .build();
//
//        // Mock de la méthode sendEmailVerification pour éviter les erreurs de rate limiting
//        doNothing().when(authService).sendEmailVerification(anyString());
//    }
//
//    @Test
//    @DisplayName("✅ Inscription réussie - Création utilisateur STUDENT")
//    void testRegister_Success_Student() {
//        // Given
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
//        when(userService.createUser(any(Utilisateur.class))).thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//        doNothing().when(emailService).sendWelcomeEmail(any(Utilisateur.class));
//
//        // When
//        AuthResponse response = authService.register(validRequest);
//
//        // Then
//        assertThat(response).isNotNull();
//        assertThat(response.getAccessToken()).isEqualTo("mock-jwt-token");
//        assertThat(response.getRefreshToken()).isEqualTo("mock-refresh-token");
//        assertThat(response.getTokenType()).isEqualTo("Bearer");
//        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");
//        assertThat(response.getUser().getRole()).isEqualTo(Role.STUDENT);
//
//        verify(userService).createUser(any(Utilisateur.class));
//        verify(passwordEncoder).encode("SecurePass123!");
//        verify(tokenService).generateJwtToken(any(Utilisateur.class));
//        verify(rateLimitService).recordRegistrationAttempt(anyString());
//    }
//
//    @Test
//    @DisplayName("✅ Inscription réussie - Création utilisateur TUTOR")
//    void testRegister_Success_Tutor() {
//        // Given
//        validRequest.setUserType("tutor");
//        mockUser.setRole(Role.TUTOR);
//
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
//        when(userService.createUser(any(Utilisateur.class))).thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//
//        // When
//        AuthResponse response = authService.register(validRequest);
//
//        // Then
//        assertThat(response.getUser().getRole()).isEqualTo(Role.TUTOR);
//    }
//
//    @Test
//    @DisplayName("✅ Inscription réussie - Création utilisateur PARENT")
//    void testRegister_Success_Parent() {
//        // Given
//        validRequest.setUserType("parent");
//        mockUser.setRole(Role.PARENT);
//
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
//        when(userService.createUser(any(Utilisateur.class))).thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//
//        // When
//        AuthResponse response = authService.register(validRequest);
//
//        // Then
//        assertThat(response.getUser().getRole()).isEqualTo(Role.PARENT);
//    }
//
//    @Test
//    @DisplayName("❌ Inscription échoue - Email déjà existant")
//    void testRegister_EmailAlreadyExists() {
//        // Given
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail("john.doe@example.com")).thenReturn(true);
//
//        // When & Then
//        assertThatThrownBy(() -> authService.register(validRequest))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Un compte avec cet email existe déjà");
//
//        verify(userService, never()).createUser(any(Utilisateur.class));
//    }
//
//    @Test
//    @DisplayName("❌ Inscription échoue - Rate limiting dépassé")
//    void testRegister_RateLimitExceeded() {
//        // Given
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(false);
//
//        // When & Then
//        assertThatThrownBy(() -> authService.register(validRequest))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Trop de tentatives d'inscription");
//
//        verify(userService, never()).existsByEmail(anyString());
//        verify(userService, never()).createUser(any(Utilisateur.class));
//    }
//
//    @Test
//    @DisplayName("✅ Mot de passe encodé correctement")
//    void testRegister_PasswordEncoded() {
//        // Given
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode("SecurePass123!")).thenReturn("super-secure-encoded-password");
//        when(userService.createUser(any(Utilisateur.class))).thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//
//        // When
//        authService.register(validRequest);
//
//        // Then
//        verify(passwordEncoder).encode("SecurePass123!");
//    }
//
//    @Test
//    @DisplayName("✅ Champs de consentement persistés correctement")
//    void testRegister_ConsentFieldsPersisted() {
//        // Given
//        validRequest.setAcceptTerms(true);
//        validRequest.setAcceptMarketing(true);
//
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
//        when(userService.createUser(argThat(user ->
//            user.getAcceptTerms() && user.getAcceptMarketing()
//        ))).thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//
//        // When
//        authService.register(validRequest);
//
//        // Then
//        verify(userService).createUser(argThat(user ->
//            user.getAcceptTerms() && user.getAcceptMarketing()
//        ));
//    }
//
//    @Test
//    @DisplayName("✅ UserType converti correctement (minuscule -> Enum)")
//    void testRegister_UserTypeConversion() {
//        // Given
//        validRequest.setUserType("STUDENT"); // Majuscule
//
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
//        when(userService.createUser(argThat(user -> user.getRole() == Role.STUDENT)))
//                .thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//
//        // When
//        authService.register(validRequest);
//
//        // Then
//        verify(userService).createUser(argThat(user -> user.getRole() == Role.STUDENT));
//    }
//
//    @Test
//    @DisplayName("✅ Statut initial PENDING_VERIFICATION")
//    void testRegister_InitialStatusPendingVerification() {
//        // Given
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
//        when(userService.createUser(argThat(user ->
//            user.getStatus() == UserStatus.PENDING_VERIFICATION
//        ))).thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//
//        // When
//        authService.register(validRequest);
//
//        // Then
//        verify(userService).createUser(argThat(user ->
//            user.getStatus() == UserStatus.PENDING_VERIFICATION &&
//            !user.getEmailVerified() &&
//            user.getLoginAttempts() == 0
//        ));
//    }
//
//    @Test
//    @DisplayName("✅ Tokens JWT et Refresh générés")
//    void testRegister_TokensGenerated() {
//        // Given
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
//        when(userService.createUser(any(Utilisateur.class))).thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//
//        // When
//        AuthResponse response = authService.register(validRequest);
//
//        // Then
//        assertThat(response.getAccessToken()).isNotNull();
//        assertThat(response.getRefreshToken()).isNotNull();
//        verify(tokenService).generateJwtToken(mockUser);
//        verify(tokenService).createRefreshToken(eq(mockUser), anyString(), anyString());
//    }
//
//    @Test
//    @DisplayName("✅ Rate limiting enregistré après inscription")
//    void testRegister_RateLimitingRecorded() {
//        // Given
//        when(rateLimitService.isRegistrationAllowed(anyString())).thenReturn(true);
//        when(userService.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
//        when(userService.createUser(any(Utilisateur.class))).thenReturn(mockUser);
//        when(tokenService.generateJwtToken(any(Utilisateur.class))).thenReturn("mock-jwt-token");
//        when(tokenService.createRefreshToken(any(Utilisateur.class), anyString(), anyString()))
//                .thenReturn(createMockRefreshToken());
//
//        // When
//        authService.register(validRequest);
//
//        // Then
//        verify(rateLimitService).recordRegistrationAttempt(anyString());
//    }
//
//    // Helper method pour créer un mock RefreshToken
//    private com.backend.tutor_app.model.support.RefreshToken createMockRefreshToken() {
//        com.backend.tutor_app.model.support.RefreshToken refreshToken =
//            new com.backend.tutor_app.model.support.RefreshToken();
//        refreshToken.setToken("mock-refresh-token");
//        return refreshToken;
//    }
}
