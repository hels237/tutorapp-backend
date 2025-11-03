package com.backend.tutor_app.model;

import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.model.support.EmailVerificationToken;
import com.backend.tutor_app.model.support.PasswordResetToken;
import com.backend.tutor_app.model.support.RefreshToken;
import com.backend.tutor_app.model.support.SocialAccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Getter
@Setter
@Entity
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type")
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur extends AbstractEntiity implements UserDetails {


    @Column(unique = true, nullable = false, length = 100)
    @Email(message = "Format d'email invalide")
    private String email;

    @Column(nullable = false)
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Le nom est requis")
    private String lastName;

    @Column(name = "phone_number", length = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Numéro de téléphone invalide")
    private String phoneNumber;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    // (PHASE 3 - Priorité 2) Champs de sécurité avancée
    @Column(name = "under_surveillance", nullable = false)
    private Boolean underSurveillance = false;
    
    @Column(name = "surveillance_started_at")
    private LocalDateTime surveillanceStartedAt;
    
    @Column(name = "compromised", nullable = false)
    private Boolean compromised = false;
    
    @Column(name = "compromised_at")
    private LocalDateTime compromisedAt;
    
    @Column(name = "compromised_reason")
    private String compromisedReason;

    // Champs de consentement (RGPD)
    @Column(name = "accept_terms", nullable = false)
    private Boolean acceptTerms = false;

    @Column(name = "accept_marketing", nullable = false)
    private Boolean acceptMarketing = false;

    // Relations
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmailVerificationToken> emailTokens = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PasswordResetToken> passwordResetTokens = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    // Méthodes utilitaires
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retourne une liste de GrantedAuthority basée sur le rôle de l'utilisateur.
        // Spring Security attend des rôles préfixés par "ROLE_".
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {return email;}

    @Override
    public String getPassword() {return password;}

    @Override
    public boolean isAccountNonExpired() {return true;}

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {return true;}

    @Override
    public boolean isEnabled() {return true;}


    //#########################################################################################





}
