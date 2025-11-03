-- PHASE 3 - Priorité 2 : Ajout des champs de sécurité avancée
-- Date: 2024-11-01
-- Description: Ajout des champs pour la gestion des comptes compromis et sous surveillance

-- Ajout des champs de sécurité dans la table utilisateur
ALTER TABLE utilisateur
ADD COLUMN IF NOT EXISTS under_surveillance BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS surveillance_started_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS compromised BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS compromised_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS compromised_reason VARCHAR(500);

-- Ajout du statut COMPROMISED dans l'enum UserStatus (si nécessaire)
-- Note: PostgreSQL ne permet pas de modifier directement un enum
-- Si vous utilisez PostgreSQL, décommentez les lignes suivantes:

-- ALTER TYPE user_status ADD VALUE IF NOT EXISTS 'COMPROMISED';

-- Pour MySQL/MariaDB, le champ status est VARCHAR donc pas de modification nécessaire

-- Index pour améliorer les performances des requêtes de sécurité
CREATE INDEX IF NOT EXISTS idx_utilisateur_under_surveillance 
ON utilisateur(under_surveillance) 
WHERE under_surveillance = TRUE;

CREATE INDEX IF NOT EXISTS idx_utilisateur_compromised 
ON utilisateur(compromised) 
WHERE compromised = TRUE;

CREATE INDEX IF NOT EXISTS idx_utilisateur_status_security 
ON utilisateur(status) 
WHERE status IN ('COMPROMISED', 'SUSPENDED', 'LOCKED');

-- Commentaires pour documentation
COMMENT ON COLUMN utilisateur.under_surveillance IS '(PHASE 3) Indique si le compte est sous surveillance pour activité suspecte';
COMMENT ON COLUMN utilisateur.surveillance_started_at IS '(PHASE 3) Date de début de la surveillance';
COMMENT ON COLUMN utilisateur.compromised IS '(PHASE 3) Indique si le compte a été compromis';
COMMENT ON COLUMN utilisateur.compromised_at IS '(PHASE 3) Date de détection du compromis';
COMMENT ON COLUMN utilisateur.compromised_reason IS '(PHASE 3) Raison du marquage comme compromis';
