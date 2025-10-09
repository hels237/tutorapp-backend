# 🔧 PLAN DE MIGRATION - TokenServiceImpl

## PROBLÈME IDENTIFIÉ
- Duplication avec JwtServiceUtil existant
- Configuration JWT différente
- Méthodes redondantes

## SOLUTION : Architecture Hybride

### AVANT (Problématique)
```java
@Service
public class TokenServiceImpl implements TokenService {
    @Value("${app.jwt.secret:mySecretKey}")
    private String jwtSecret;
    
    public String generateJwtToken(User user) {
        // Code dupliqué avec JwtServiceUtil
    }
    
    public boolean validateJwtToken(String token) {
        // Code dupliqué avec JwtServiceUtil
    }
}
```

### APRÈS (Solution Intégrée)
```java
@Service
public class TokenServiceImpl implements TokenService {
    
    // INTÉGRATION avec service existant
    private final JwtServiceUtil jwtServiceUtil;
    
    // DÉLÉGATION pour JWT
    public String generateJwtToken(User user) {
        return jwtServiceUtil.generateToken(user);
    }
    
    public boolean validateJwtToken(String token) {
        // Adapter la validation existante
        UserDetails userDetails = customUserService.loadUserByUsername(
            jwtServiceUtil.extractUsername(token)
        );
        return jwtServiceUtil.validateToken(token, userDetails);
    }
    
    // CONSERVATION des fonctionnalités uniques
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        // Garder cette logique métier
    }
    
    public EmailVerificationToken createEmailVerificationToken(User user, String ipAddress) {
        // Garder cette logique métier
    }
}
```

## ACTIONS CONCRÈTES

### 1. Supprimer les doublons JWT
- ❌ Supprimer `generateJwtToken()` custom
- ❌ Supprimer `validateJwtToken()` custom  
- ❌ Supprimer configuration JWT custom

### 2. Intégrer avec JwtServiceUtil
- ✅ Injecter `JwtServiceUtil jwtServiceUtil`
- ✅ Injecter `CustomUserService customUserService`
- ✅ Déléguer génération JWT à `jwtServiceUtil.generateToken()`
- ✅ Adapter validation avec `jwtServiceUtil.validateToken()`

### 3. Conserver les fonctionnalités uniques
- ✅ Garder gestion Refresh Tokens
- ✅ Garder gestion Email Verification Tokens
- ✅ Garder gestion Password Reset Tokens
- ✅ Garder méthodes utilitaires (cleanup, stats)

### 4. Adapter les méthodes d'extraction
```java
// AVANT
public Long getUserIdFromJwtToken(String token) {
    Claims claims = Jwts.parserBuilder()...
    return claims.get("userId", Long.class);
}

// APRÈS  
public Long getUserIdFromJwtToken(String token) {
    return jwtServiceUtil.extractClaim(token, claims -> claims.get("id", Long.class));
}
```
