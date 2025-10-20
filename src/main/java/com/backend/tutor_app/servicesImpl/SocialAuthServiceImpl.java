package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.dto.Auth.AuthResponse;
import com.backend.tutor_app.dto.user.UserProfileDto;
import com.backend.tutor_app.model.Utilisateur;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.SocialProvider;
import com.backend.tutor_app.model.enums.UserStatus;
import com.backend.tutor_app.model.support.SocialAccount;
import com.backend.tutor_app.repositories.SocialAccountRepository;
import com.backend.tutor_app.services.SocialAuthService;
import com.backend.tutor_app.services.TokenService;
import com.backend.tutor_app.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Implémentation du service d'authentification sociale pour TutorApp
 * Gère l'authentification OAuth2 avec Google, Facebook, GitHub
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialAuthServiceImpl implements SocialAuthService {

    // INTÉGRATION avec architecture existante
    private final SocialAccountRepository socialAccountRepository;
    private final UserService userService;
    private final TokenService tokenService;  // ← Refactorisé pour déléguer à JwtServiceUtil
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth2.google.client-secret:}")
    private String googleClientSecret;

    @Value("${app.oauth2.facebook.client-id:}")
    private String facebookClientId;

    @Value("${app.oauth2.facebook.client-secret:}")
    private String facebookClientSecret;

    @Value("${app.oauth2.github.client-id:}")
    private String githubClientId;

    @Value("${app.oauth2.github.client-secret:}")
    private String githubClientSecret;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public AuthResponse authenticateWithSocialProvider(SocialProvider provider, String authorizationCode) {
        log.info("Authentification sociale avec {}", provider);
        
        try {
            String redirectUri = frontendUrl + "/auth/callback/" + provider.name().toLowerCase();
            String accessToken = exchangeCodeForAccessToken(provider, authorizationCode, redirectUri);
            Map<String, Object> userInfo = getUserInfoFromProvider(provider, accessToken);
            
            if (!validateProviderData(provider, userInfo)) {
                throw new RuntimeException("Données utilisateur invalides");
            }
            
            String providerId = extractProviderId(provider, userInfo);
            String email = (String) userInfo.get("email");
            
            Optional<SocialAccount> existingSocialAccount = findSocialAccount(provider, providerId);
            
            Utilisateur utilisateur;
            if (existingSocialAccount.isPresent()) {
                utilisateur = existingSocialAccount.get().getUtilisateur();
                updateSocialAccountTokens(existingSocialAccount.get(), accessToken, userInfo);
            } else {
                Optional<Utilisateur> existingUser = userService.getUserByEmail(email);
                if (existingUser.isPresent()) {
                    utilisateur = existingUser.get();
                    createOrUpdateSocialAccount(utilisateur, provider, userInfo, accessToken, null);
                } else {
                    utilisateur = createUserFromSocialProvider(provider, userInfo);
                    createOrUpdateSocialAccount(utilisateur, provider, userInfo, accessToken, null);
                }
            }
            
            // Génération des tokens JWT (délégation vers JwtServiceUtil via TokenService refactorisé)
            String jwtToken = tokenService.generateJwtToken(utilisateur);  // ← Délègue à JwtServiceUtil.generateToken()
            String refreshToken = tokenService.createRefreshToken(utilisateur, "Social Login", getCurrentClientIp()).getToken();
            
            return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(UserProfileDto.fromEntity(utilisateur))
                .build();
                
        } catch (Exception e) {
            log.error("Erreur authentification sociale avec {} - {}", provider, e.getMessage());
            throw new RuntimeException("Erreur authentification sociale: " + e.getMessage());
        }
    }

    @Override
    public String generateAuthorizationUrl(SocialProvider provider, String redirectUri) {
        return switch (provider) {
            case GOOGLE -> String.format(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid email profile",
                googleClientId, redirectUri
            );
            case FACEBOOK -> String.format(
                "https://www.facebook.com/v18.0/dialog/oauth?client_id=%s&redirect_uri=%s&response_type=code&scope=email",
                facebookClientId, redirectUri
            );
            case GITHUB -> String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=user:email",
                githubClientId, redirectUri
            );
        };
    }

    @Override
    public String exchangeCodeForAccessToken(SocialProvider provider, String authorizationCode, String redirectUri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            String tokenUrl;
            String requestBody;
            
            switch (provider) {
                case GOOGLE -> {
                    tokenUrl = "https://oauth2.googleapis.com/token";
                    requestBody = String.format(
                        "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s",
                        googleClientId, googleClientSecret, authorizationCode, redirectUri
                    );
                }
                case FACEBOOK -> {
                    tokenUrl = "https://graph.facebook.com/v18.0/oauth/access_token";
                    requestBody = String.format(
                        "client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                        facebookClientId, facebookClientSecret, authorizationCode, redirectUri
                    );
                }
                case GITHUB -> {
                    tokenUrl = "https://github.com/login/oauth/access_token";
                    requestBody = String.format(
                        "client_id=%s&client_secret=%s&code=%s",
                        githubClientId, githubClientSecret, authorizationCode
                    );
                }
                default -> throw new RuntimeException("Provider non supporté: " + provider);
            }
            
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
            
            throw new RuntimeException("Échec de l'échange du code");
            
        } catch (Exception e) {
            log.error("Erreur échange code pour token - {}", e.getMessage());
            throw new RuntimeException("Erreur échange code: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserInfoFromProvider(SocialProvider provider, String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String userInfoUrl = switch (provider) {
                case GOOGLE -> "https://www.googleapis.com/oauth2/v2/userinfo";
                case FACEBOOK -> "https://graph.facebook.com/me?fields=id,name,email,first_name,last_name";
                case GITHUB -> "https://api.github.com/user";
            };
            
            ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
            throw new RuntimeException("Échec récupération infos utilisateur");
            
        } catch (Exception e) {
            log.error("Erreur récupération infos utilisateur - {}", e.getMessage());
            throw new RuntimeException("Erreur récupération infos: " + e.getMessage());
        }
    }

    @Override
    public SocialAccount createOrUpdateSocialAccount(Utilisateur utilisateur, SocialProvider provider, Map<String, Object> providerData,
                                                     String accessToken, String refreshToken) {
        try {
            String providerId = extractProviderId(provider, providerData);
            
            Optional<SocialAccount> existingAccount = socialAccountRepository.findByUtilisateurAndProvider(utilisateur, provider);
            
            SocialAccount socialAccount;
            if (existingAccount.isPresent()) {
                socialAccount = existingAccount.get();
            } else {
                socialAccount = new SocialAccount();
                socialAccount.setUtilisateur(utilisateur);
                socialAccount.setProvider(provider);
                socialAccount.setProviderId(providerId);
            }
            
            socialAccount.setProviderEmail((String) providerData.get("email"));
            socialAccount.setProviderName(extractProviderName(provider, providerData));
            socialAccount.setAccessToken(accessToken);
            socialAccount.setRefreshToken(refreshToken);
            socialAccount.setTokenExpiresAt(LocalDateTime.now().plusHours(1));
            
            return socialAccountRepository.save(socialAccount);
            
        } catch (Exception e) {
            log.error("Erreur création/mise à jour compte social - {}", e.getMessage());
            throw new RuntimeException("Erreur compte social: " + e.getMessage());
        }
    }

    @Override
    public Optional<SocialAccount> findSocialAccount(SocialProvider provider, String providerId) {
        return socialAccountRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public List<SocialAccount> getUserSocialAccounts(Long userId) {
        try {
            Utilisateur utilisateur = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            return socialAccountRepository.findByUtilisateur(utilisateur);
        } catch (Exception e) {
            log.error("Erreur récupération comptes sociaux utilisateur {} - {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public SocialAccount linkSocialAccount(Long userId, SocialProvider provider, String authorizationCode) {
        try {
            Utilisateur utilisateur = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            if (hasUserSocialAccount(userId, provider)) {
                throw new RuntimeException("Compte social déjà lié");
            }
            
            String redirectUri = frontendUrl + "/auth/link/" + provider.name().toLowerCase();
            String accessToken = exchangeCodeForAccessToken(provider, authorizationCode, redirectUri);
            Map<String, Object> userInfo = getUserInfoFromProvider(provider, accessToken);
            
            return createOrUpdateSocialAccount(utilisateur, provider, userInfo, accessToken, null);
            
        } catch (Exception e) {
            log.error("Erreur liaison compte social - {}", e.getMessage());
            throw new RuntimeException("Erreur liaison compte: " + e.getMessage());
        }
    }

    @Override
    public void unlinkSocialAccount(Long userId, SocialProvider provider) {
        try {
            Utilisateur utilisateur = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            Optional<SocialAccount> socialAccount = socialAccountRepository.findByUtilisateurAndProvider(utilisateur, provider);
            if (socialAccount.isPresent()) {
                socialAccountRepository.delete(socialAccount.get());
                log.info("Compte social {} délié pour utilisateur {}", provider, userId);
            }
            
        } catch (Exception e) {
            log.error("Erreur déliaison compte social - {}", e.getMessage());
            throw new RuntimeException("Erreur déliaison compte: " + e.getMessage());
        }
    }

    @Override
    public boolean hasUserSocialAccount(Long userId, SocialProvider provider) {
        try {
            Utilisateur utilisateur = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            return socialAccountRepository.existsByUtilisateurAndProvider(utilisateur, provider);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String refreshSocialAccessToken(Long socialAccountId) {
        // TODO: Implémenter le rafraîchissement des tokens OAuth2
        throw new RuntimeException("Rafraîchissement token non implémenté");
    }

    @Override
    public boolean isSocialAccessTokenExpired(Long socialAccountId) {
        try {
            Optional<SocialAccount> account = socialAccountRepository.findById(socialAccountId);
            if (account.isPresent()) {
                return account.get().getTokenExpiresAt().isBefore(LocalDateTime.now());
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void revokeSocialAccess(Long socialAccountId) {
        try {
            Optional<SocialAccount> account = socialAccountRepository.findById(socialAccountId);
            if (account.isPresent()) {
                socialAccountRepository.delete(account.get());
                log.info("Accès social révoqué pour compte {}", socialAccountId);
            }
        } catch (Exception e) {
            log.error("Erreur révocation accès social - {}", e.getMessage());
        }
    }

    @Override
    public Utilisateur createUserFromSocialProvider(SocialProvider provider, Map<String, Object> providerData) {
        try {
            String email = (String) providerData.get("email");
            String firstName = extractFirstName(provider, providerData);
            String lastName = extractLastName(provider, providerData);
            
            Utilisateur utilisateur = Utilisateur.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .emailVerified(true) // Email vérifié par le provider OAuth2
                .loginAttempts(0)
                .passwordChangedAt(LocalDateTime.now())
                .build();
            
            return userService.createUser(utilisateur);
            
        } catch (Exception e) {
            log.error("Erreur création utilisateur depuis provider social - {}", e.getMessage());
            throw new RuntimeException("Erreur création utilisateur: " + e.getMessage());
        }
    }

    @Override
    public Utilisateur updateUserFromSocialProvider(Utilisateur utilisateur, SocialProvider provider, Map<String, Object> providerData) {
        try {
            String firstName = extractFirstName(provider, providerData);
            String lastName = extractLastName(provider, providerData);
            
            if (firstName != null && !firstName.equals(utilisateur.getFirstName())) {
                utilisateur.setFirstName(firstName);
            }
            
            if (lastName != null && !lastName.equals(utilisateur.getLastName())) {
                utilisateur.setLastName(lastName);
            }
            
            return userService.updateUser(utilisateur.getId(), utilisateur);
            
        } catch (Exception e) {
            log.error("Erreur mise à jour utilisateur depuis provider - {}", e.getMessage());
            return utilisateur;
        }
    }

    @Override
    public boolean validateProviderData(SocialProvider provider, Map<String, Object> providerData) {
        if (providerData == null || providerData.isEmpty()) {
            return false;
        }
        
        String email = (String) providerData.get("email");
        String id = extractProviderId(provider, providerData);
        
        return email != null && !email.trim().isEmpty() && 
               id != null && !id.trim().isEmpty();
    }

    @Override
    public boolean isSupportedProvider(SocialProvider provider) {
        return provider == SocialProvider.GOOGLE || 
               provider == SocialProvider.FACEBOOK || 
               provider == SocialProvider.GITHUB;
    }

    @Override
    public Map<String, Object> getSocialAuthStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            long totalAccounts = socialAccountRepository.countTotalSocialAccounts();
            stats.put("totalSocialAccounts", totalAccounts);
            
            Map<SocialProvider, Long> providerStats = getUserCountByProvider();
            stats.put("usersByProvider", providerStats);
            
            return stats;
            
        } catch (Exception e) {
            log.error("Erreur récupération statistiques auth sociale - {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public Map<SocialProvider, Long> getUserCountByProvider() {
        try {
            Map<SocialProvider, Long> stats = new HashMap<>();
            for (SocialProvider provider : SocialProvider.values()) {
                long count = socialAccountRepository.countByProvider(provider);
                stats.put(provider, count);
            }
            return stats;
        } catch (Exception e) {
            log.error("Erreur comptage utilisateurs par provider - {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public void syncSocialAccountData(Long socialAccountId) {
        // TODO: Implémenter la synchronisation des données
        log.info("Synchronisation compte social {} (non implémentée)", socialAccountId);
    }

    @Override
    public void syncAllUserSocialAccounts(Long userId) {
        try {
            List<SocialAccount> accounts = getUserSocialAccounts(userId);
            for (SocialAccount account : accounts) {
                syncSocialAccountData(account.getId());
            }
        } catch (Exception e) {
            log.error("Erreur synchronisation comptes sociaux utilisateur {} - {}", userId, e.getMessage());
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private String extractProviderId(SocialProvider provider, Map<String, Object> providerData) {
        return switch (provider) {
            case GOOGLE, FACEBOOK, GITHUB -> (String) providerData.get("id");
        };
    }

    private String extractProviderName(SocialProvider provider, Map<String, Object> providerData) {
        return switch (provider) {
            case GOOGLE, GITHUB -> (String) providerData.get("name");
            case FACEBOOK -> (String) providerData.get("name");
        };
    }

    private String extractFirstName(SocialProvider provider, Map<String, Object> providerData) {
        return switch (provider) {
            case GOOGLE -> (String) providerData.get("given_name");
            case FACEBOOK -> (String) providerData.get("first_name");
            case GITHUB -> {
                String name = (String) providerData.get("name");
                yield name != null ? name.split(" ")[0] : "Utilisateur";
            }
        };
    }

    private String extractLastName(SocialProvider provider, Map<String, Object> providerData) {
        return switch (provider) {
            case GOOGLE -> (String) providerData.get("family_name");
            case FACEBOOK -> (String) providerData.get("last_name");
            case GITHUB -> {
                String name = (String) providerData.get("name");
                if (name != null && name.contains(" ")) {
                    String[] parts = name.split(" ");
                    yield parts.length > 1 ? parts[parts.length - 1] : "";
                }
                yield "";
            }
        };
    }

    private void updateSocialAccountTokens(SocialAccount account, String accessToken, Map<String, Object> userInfo) {
        account.setAccessToken(accessToken);
        account.setTokenExpiresAt(LocalDateTime.now().plusHours(1));
        account.setProviderEmail((String) userInfo.get("email"));
        socialAccountRepository.save(account);
    }

    private String getCurrentClientIp() {
        return "127.0.0.1"; // TODO: Récupérer la vraie IP client
    }

    private Object mapUserToDto(Utilisateur utilisateur) {
        return Map.of(
            "id", utilisateur.getId(),
            "email", utilisateur.getEmail(),
            "firstName", utilisateur.getFirstName(),
            "lastName", utilisateur.getLastName(),
            "role", utilisateur.getRole(),
            "status", utilisateur.getStatus(),
            "emailVerified", utilisateur.getEmailVerified()
        );
    }
}
