-- PHASE 3 - Priorité 3 : Ajout de la table security_confirmation_token
-- Date: 2024-11-01
-- Description: Table pour gérer les tokens de confirmation de sécurité après détection d'activité suspecte

CREATE TABLE IF NOT EXISTS security_confirmation_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    reason VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    confirmation_ip VARCHAR(45),
    confirmation_user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_security_confirmation_utilisateur 
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id) 
        ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_security_confirmation_token ON security_confirmation_token(token);
CREATE INDEX idx_security_confirmation_utilisateur ON security_confirmation_token(utilisateur_id);
CREATE INDEX idx_security_confirmation_expires ON security_confirmation_token(expires_at);
CREATE INDEX idx_security_confirmation_used ON security_confirmation_token(is_used) WHERE is_used = FALSE;

-- Index composé pour les requêtes de validation
CREATE INDEX idx_security_confirmation_valid 
ON security_confirmation_token(utilisateur_id, is_used, expires_at);

-- Commentaires pour documentation
COMMENT ON TABLE security_confirmation_token IS '(PHASE 3) Tokens de confirmation de sécurité après activité suspecte';
COMMENT ON COLUMN security_confirmation_token.token IS 'Token unique de confirmation (UUID)';
COMMENT ON COLUMN security_confirmation_token.expires_at IS 'Date d expiration du token (généralement 30 minutes)';
COMMENT ON COLUMN security_confirmation_token.confirmed_at IS 'Date de confirmation par l utilisateur';
COMMENT ON COLUMN security_confirmation_token.is_used IS 'Indique si le token a été utilisé';
COMMENT ON COLUMN security_confirmation_token.reason IS 'Raison de la demande de confirmation';
COMMENT ON COLUMN security_confirmation_token.ip_address IS 'IP de l activité suspecte';
COMMENT ON COLUMN security_confirmation_token.confirmation_ip IS 'IP utilisée pour la confirmation';
