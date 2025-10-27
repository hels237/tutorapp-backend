-- (Q) PHASE 1 - ÉTAPE 1.2 : Migration pour ajouter les métadonnées enrichies à la table refresh_tokens
-- Cette migration ajoute les colonnes nécessaires pour la traçabilité et la sécurité avancée

-- Ajout des colonnes de métadonnées enrichies
ALTER TABLE refresh_tokens
ADD COLUMN IF NOT EXISTS usage_count INTEGER DEFAULT 0 NOT NULL,
ADD COLUMN IF NOT EXISTS parent_token_id BIGINT,
ADD COLUMN IF NOT EXISTS browser_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS browser_version VARCHAR(50),
ADD COLUMN IF NOT EXISTS os_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS os_version VARCHAR(50),
ADD COLUMN IF NOT EXISTS timezone VARCHAR(100),
ADD COLUMN IF NOT EXISTS browser_language VARCHAR(10),
ADD COLUMN IF NOT EXISTS user_agent VARCHAR(1000),
ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS revoked_reason VARCHAR(255);

-- Ajout d'un index sur parent_token_id pour la traçabilité de la chaîne de rotation
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_parent_token_id ON refresh_tokens(parent_token_id);

-- Ajout d'un index sur revoked_at pour les requêtes de nettoyage
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked_at ON refresh_tokens(revoked_at);

-- Ajout d'un index composite pour les requêtes de sécurité (IP + device)
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_ip_device ON refresh_tokens(ip_address, browser_name);

-- Commentaires pour documentation
COMMENT ON COLUMN refresh_tokens.usage_count IS '(Q) PHASE 1 - Nombre de fois que le token a été utilisé pour le refresh';
COMMENT ON COLUMN refresh_tokens.parent_token_id IS '(Q) PHASE 1 - ID du token parent pour tracer la chaîne de rotation';
COMMENT ON COLUMN refresh_tokens.browser_name IS '(Q) PHASE 1 - Nom du navigateur (Chrome, Firefox, Safari, etc.)';
COMMENT ON COLUMN refresh_tokens.browser_version IS '(Q) PHASE 1 - Version du navigateur';
COMMENT ON COLUMN refresh_tokens.os_name IS '(Q) PHASE 1 - Système d''exploitation (Windows, macOS, Linux, etc.)';
COMMENT ON COLUMN refresh_tokens.os_version IS '(Q) PHASE 1 - Version du système d''exploitation';
COMMENT ON COLUMN refresh_tokens.timezone IS '(Q) PHASE 1 - Fuseau horaire du client (Europe/Paris, etc.)';
COMMENT ON COLUMN refresh_tokens.browser_language IS '(Q) PHASE 1 - Langue du navigateur (fr-FR, en-US, etc.)';
COMMENT ON COLUMN refresh_tokens.user_agent IS '(Q) PHASE 1 - User Agent complet pour analyse détaillée';
COMMENT ON COLUMN refresh_tokens.revoked_at IS '(Q) PHASE 1 - Date de révocation du token pour audit';
COMMENT ON COLUMN refresh_tokens.revoked_reason IS '(Q) PHASE 1 - Raison de la révocation (ROTATED, LOGOUT, SECURITY, etc.)';
