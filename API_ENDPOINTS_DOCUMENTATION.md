# 📚 DOCUMENTATION COMPLÈTE DES ENDPOINTS API - TutorApp Backend

## 🎯 RÉSUMÉ GLOBAL

Votre backend expose **60+ endpoints** répartis en **4 contrôleurs** :

| Contrôleur | Base URL | Endpoints | Accès | Status |
|------------|----------|-----------|-------|--------|
| **AuthController** | `/api/v1/auth` | 10 | Public + Auth | ✅ Prêt |
| **UserController** | `/api/v1/utilisateurs` | 17 | Authentifié | ✅ Prêt |
| **SocialAuthController** | `/api/v1/auth/social` | 13 | Public + Auth | ✅ Prêt |
| **AdminController** | `/api/v1/admin` | 20+ | Admin only | ✅ Prêt |

---

## 🔐 1. AUTHENTIFICATION (`/api/v1/auth`)

### Liste des Endpoints

| Méthode | Endpoint | Page Frontend | Description |
|---------|----------|---------------|-------------|
| POST | `/login` | `/login` | Connexion email/password |
| POST | `/register` | `/register` | Inscription multi-rôles |
| POST | `/logout` | Bouton header | Déconnexion |
| POST | `/refresh` | Auto | Renouvelle JWT |
| POST | `/forgot-password` | `/forgot-password` | Demande reset |
| POST | `/reset-password` | `/reset-password` | Reset password |
| GET | `/verify-email` | Lien email | Vérifie email |
| POST | `/resend-verification` | Bouton | Renvoie email |
| GET | `/validate-token` | Middleware | Valide JWT |
| GET | `/me` | Dashboard | Info utilisateur connecté |

### POST `/api/v1/auth/login` - Connexion

**Request:**
```json
{
  "email": "utilisateur@example.com",
  "password": "SecurePass123!"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "uuid-token",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "utilisateur": {
      "id": 1,
      "email": "utilisateur@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "STUDENT",
      "emailVerified": true
    }
  },
  "message": "Connexion réussie"
}
```

**Fonctionnalités:**
- ✅ Rate limiting (5 tentatives max)
- ✅ Verrouillage compte après échecs
- ✅ JWT + Refresh Token

### POST `/api/v1/auth/register` - Inscription

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "userType": "STUDENT",
  "phoneNumber": "+33612345678"
}
```

**Response 201:**
```json
{
  "success": true,
  "data": {
    "accessToken": "jwt-token",
    "utilisateur": {
      "id": 2,
      "email": "john@example.com",
      "role": "STUDENT",
      "status": "PENDING_VERIFICATION"
    }
  },
  "message": "Inscription réussie. Vérifiez votre email."
}
```

---

## 👤 2. GESTION UTILISATEURS (`/api/v1/utilisateurs`)

### Liste des Endpoints

| Méthode | Endpoint | Page Frontend | Description |
|---------|----------|---------------|-------------|
| GET | `/profile` | Dashboard | Mon profil |
| PUT | `/profile` | Paramètres | Modifier profil |
| POST | `/profile/picture` | Upload | Photo profil |
| DELETE | `/profile/picture` | - | Supprimer photo |
| PUT | `/password` | Sécurité | Changer password |
| GET | `/security-info` | Dashboard | Infos sécurité |
| GET | `/search` | Recherche | Chercher utilisateurs |
| GET | `/tutors` | `/tutors` | Liste tuteurs |
| GET | `/{id}` | `/tutors/{id}` | Profil public |
| POST | `/become-tutor` | `/become-tutor` | Demande tuteur |
| GET | `/tutor-application-status` | Dashboard | Statut demande |
| GET | `/stats` | Dashboard | Mes stats |
| GET | `/preferences` | Paramètres | Préférences |
| PUT | `/preferences` | Paramètres | Modifier préf |
| POST | `/deactivate` | - | Désactiver compte |
| POST | `/reactivate` | - | Réactiver compte |
| DELETE | `/account` | - | Supprimer (GDPR) |

### GET `/api/v1/utilisateurs/profile` - Mon profil

**Headers:**
```
Authorization: Bearer eyJhbGc...
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "utilisateur@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+33612345678",
    "profilePicture": "url",
    "role": "STUDENT",
    "status": "ACTIVE",
    "emailVerified": true
  },
  "message": "Profil utilisateur récupéré"
}
```

### GET `/api/v1/utilisateurs/tutors` - Liste tuteurs

**Query Params:**
- `subject` : Matière (math, physics, etc.)
- `minPrice`, `maxPrice` : Fourchette de prix
- `minRating` : Note minimale
- `page`, `size` : Pagination

**Exemple:**
```
GET /api/v1/utilisateurs/tutors?subject=math&minPrice=15&maxPrice=30&page=0&size=12
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 5,
        "firstName": "Marie",
        "lastName": "Dupont",
        "hourlyRate": 25.00,
        "rating": 4.8,
        "experienceYears": 5
      }
    ],
    "totalElements": 28,
    "totalPages": 3
  }
}
```

### POST `/api/v1/utilisateurs/become-tutor` - Devenir tuteur

**Headers:**
```
Authorization: Bearer eyJhbGc...
Content-Type: multipart/form-data
```

**Form Data:**
- `data` : JSON avec infos tuteur
- `documents[]` : Fichiers (diplômes)

**Response 200:**
```json
{
  "success": true,
  "data": {
    "status": "pending",
    "applicationId": 123
  },
  "message": "Demande soumise avec succès"
}
```

---

## 🌐 3. AUTHENTIFICATION SOCIALE (`/api/v1/auth/social`)

### Liste des Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/google/authorize` | URL autorisation Google |
| POST | `/google/callback` | Callback Google |
| GET | `/facebook/authorize` | URL Facebook |
| POST | `/facebook/callback` | Callback Facebook |
| GET | `/github/authorize` | URL GitHub |
| POST | `/github/callback` | Callback GitHub |
| POST | `/google/link` | Lier compte Google |
| POST | `/facebook/link` | Lier Facebook |
| POST | `/github/link` | Lier GitHub |
| GET | `/accounts` | Mes comptes liés |
| DELETE | `/{provider}/unlink` | Délier compte |

### GET `/api/v1/auth/social/google/authorize` - URL Google

**Response 200:**
```json
{
  "success": true,
  "data": {
    "authUrl": "https://accounts.google.com/o/oauth2/v2/auth?...",
    "provider": "google"
  }
}
```

### POST `/api/v1/auth/social/google/callback` - Callback Google

**Request:**
```json
{
  "code": "authorization-code-from-google"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "accessToken": "jwt-token",
    "utilisateur": {
      "id": 10,
      "email": "utilisateur@gmail.com",
      "profilePicture": "https://lh3.googleusercontent.com/..."
    }
  },
  "message": "Connexion GOOGLE réussie"
}
```

---

## 👨‍💼 4. ADMINISTRATION (`/api/v1/admin`)

**Accès:** ADMIN uniquement

### Liste des Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/dashboard` | Stats globales |
| GET | `/stats/overview` | Stats détaillées |
| GET | `/utilisateurs` | Liste complète utilisateurs |
| GET | `/utilisateurs/{id}` | Détails utilisateur |
| PUT | `/utilisateurs/{id}/status` | Modifier statut |
| PUT | `/utilisateurs/{id}/role` | Modifier rôle |
| DELETE | `/utilisateurs/{id}` | Supprimer utilisateur |
| GET | `/tutor-applications` | Demandes tuteur |
| PUT | `/tutor-applications/{id}/approve` | Approuver |
| PUT | `/tutor-applications/{id}/reject` | Rejeter |
| GET | `/moderation/reports` | Signalements |
| PUT | `/moderation/reports/{id}/resolve` | Résoudre |
| GET | `/system/config` | Config système |
| PUT | `/system/config` | Modifier config |
| GET | `/audit/logs` | Logs audit |

### GET `/api/v1/admin/dashboard` - Dashboard admin

**Response 200:**
```json
{
  "success": true,
  "data": {
    "totalUsers": 1250,
    "activeUsers": 980,
    "totalTutors": 145,
    "pendingTutorApplications": 12,
    "revenue": {
      "today": 1250.50,
      "thisMonth": 45680.00
    }
  }
}
```

### GET `/api/v1/admin/utilisateurs` - Liste utilisateurs

**Query Params:**
- `search` : Recherche
- `role` : STUDENT, TUTOR, PARENT, ADMIN
- `status` : ACTIVE, INACTIVE, SUSPENDED
- `page`, `size` : Pagination

**Exemple:**
```
GET /api/v1/admin/utilisateurs?role=TUTOR&status=ACTIVE&page=0&size=50
```

### PUT `/api/v1/admin/tutor-applications/{id}/approve` - Approuver

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "status": "APPROVED"
  },
  "message": "Demande approuvée"
}
```

**Fonctionnalités:**
- ✅ Change le rôle en TUTOR
- ✅ Active le profil tuteur
- ✅ Envoie email de confirmation

---

## 📝 FORMAT RÉPONSE STANDARD

**Succès:**
```json
{
  "success": true,
  "data": { /* Données */ },
  "message": "Message descriptif"
}
```

**Erreur:**
```json
{
  "success": false,
  "data": null,
  "message": "Message d'erreur"
}
```

---

## 🔑 AUTHENTIFICATION

**Header requis:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Codes HTTP:**
- `200` : Succès
- `201` : Créé
- `400` : Erreur requête
- `401` : Non authentifié
- `403` : Non autorisé
- `404` : Non trouvé
- `429` : Rate limit
- `500` : Erreur serveur

---

## ✅ RÉSUMÉ - ENDPOINTS PRÊTS

**Totalement fonctionnels:**
- ✅ Inscription/Connexion/Déconnexion
- ✅ Gestion profil utilisateur
- ✅ Upload photo de profil
- ✅ Recherche utilisateurs/tuteurs
- ✅ Devenir tuteur
- ✅ OAuth2 (Google/Facebook/GitHub)
- ✅ Administration complète
- ✅ Rate limiting & sécurité

**Tous les endpoints sont opérationnels et prêts à être consommés par votre frontend Next.js !** 🎉
