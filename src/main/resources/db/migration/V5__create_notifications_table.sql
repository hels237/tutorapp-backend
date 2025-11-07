-- Migration V5: Création de la table notifications
-- Date: 2025-01-07
-- Description: Table pour stocker les notifications utilisateur (WebSocket, FCM, Email)

-- ==================== TABLE NOTIFICATIONS ====================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    metadata JSONB,
    action_url VARCHAR(500),
    action_label VARCHAR(100),
    icon_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    sent_via_websocket BOOLEAN DEFAULT FALSE,
    sent_via_fcm BOOLEAN DEFAULT FALSE,
    sent_via_email BOOLEAN DEFAULT FALSE,
    
    -- Contrainte de clé étrangère
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) 
        REFERENCES utilisateur(id) ON DELETE CASCADE,
    
    -- Contrainte de vérification du type
    CONSTRAINT chk_notification_type CHECK (type IN (
        'SECURITY_ALERT', 'SECURITY_LOGIN', 'SECURITY_PASSWORD_CHANGED', 'SECURITY_ACCOUNT_LOCKED',
        'NEW_BOOKING', 'BOOKING_CONFIRMED', 'BOOKING_CANCELLED', 'BOOKING_REMINDER', 'BOOKING_COMPLETED',
        'NEW_MESSAGE', 'MESSAGE_REPLY',
        'PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'PAYMENT_REFUND',
        'SYSTEM_UPDATE', 'SYSTEM_MAINTENANCE', 'SYSTEM_ANNOUNCEMENT',
        'TUTOR_APPLICATION_APPROVED', 'TUTOR_APPLICATION_REJECTED', 'TUTOR_DOCUMENT_VERIFIED', 
        'TUTOR_DOCUMENT_REJECTED', 'TUTOR_NEW_REVIEW',
        'STUDENT_LESSON_REMINDER', 'STUDENT_HOMEWORK_ASSIGNED', 'STUDENT_GRADE_POSTED',
        'PARENT_CHILD_ACTIVITY', 'PARENT_PAYMENT_DUE',
        'ADMIN_NEW_USER', 'ADMIN_REPORT_SUBMITTED', 'ADMIN_ACTION_REQUIRED'
    )),
    
    -- Contrainte de vérification de la priorité
    CONSTRAINT chk_notification_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- ==================== INDEX POUR PERFORMANCES ====================

-- Index sur user_id pour récupération rapide des notifications d'un utilisateur
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notifications(user_id);

-- Index composite pour récupération des notifications non lues
CREATE INDEX IF NOT EXISTS idx_notification_user_read ON notifications(user_id, is_read);

-- Index sur created_at pour tri chronologique
CREATE INDEX IF NOT EXISTS idx_notification_created_at ON notifications(created_at DESC);

-- Index sur type pour filtrage par type
CREATE INDEX IF NOT EXISTS idx_notification_type ON notifications(type);

-- Index sur priority pour filtrage par priorité
CREATE INDEX IF NOT EXISTS idx_notification_priority ON notifications(priority);

-- Index composite pour les notifications récentes non lues (cas d'usage fréquent)
CREATE INDEX IF NOT EXISTS idx_notification_user_unread_recent 
    ON notifications(user_id, is_read, created_at DESC) 
    WHERE is_read = FALSE;

-- Index GIN sur metadata pour recherche dans le JSON (PostgreSQL uniquement)
CREATE INDEX IF NOT EXISTS idx_notification_metadata ON notifications USING GIN (metadata);

-- ==================== COMMENTAIRES ====================

COMMENT ON TABLE notifications IS 'Table des notifications utilisateur (WebSocket, FCM, Email)';
COMMENT ON COLUMN notifications.id IS 'Identifiant unique de la notification';
COMMENT ON COLUMN notifications.user_id IS 'ID de l''utilisateur destinataire';
COMMENT ON COLUMN notifications.type IS 'Type de notification (SECURITY_ALERT, NEW_BOOKING, etc.)';
COMMENT ON COLUMN notifications.priority IS 'Priorité de la notification (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN notifications.title IS 'Titre de la notification';
COMMENT ON COLUMN notifications.message IS 'Message de la notification';
COMMENT ON COLUMN notifications.metadata IS 'Métadonnées additionnelles au format JSON';
COMMENT ON COLUMN notifications.action_url IS 'URL de l''action à effectuer (optionnel)';
COMMENT ON COLUMN notifications.action_label IS 'Label du bouton d''action (optionnel)';
COMMENT ON COLUMN notifications.icon_url IS 'URL de l''icône (optionnel)';
COMMENT ON COLUMN notifications.is_read IS 'Indique si la notification a été lue';
COMMENT ON COLUMN notifications.created_at IS 'Date de création de la notification';
COMMENT ON COLUMN notifications.read_at IS 'Date de lecture de la notification';
COMMENT ON COLUMN notifications.sent_via_websocket IS 'Indique si envoyée via WebSocket';
COMMENT ON COLUMN notifications.sent_via_fcm IS 'Indique si envoyée via FCM (Firebase Cloud Messaging)';
COMMENT ON COLUMN notifications.sent_via_email IS 'Indique si envoyée par email';

-- ==================== DONNÉES DE TEST (OPTIONNEL) ====================

-- Exemple de notification de sécurité
-- INSERT INTO notifications (user_id, type, priority, title, message, metadata, action_url, action_label, icon_url)
-- VALUES (
--     1,
--     'SECURITY_LOGIN',
--     'HIGH',
--     'Nouvelle connexion détectée',
--     'Une connexion à votre compte a été détectée depuis Paris, France',
--     '{"ip": "192.168.1.1", "country": "France", "city": "Paris", "device": "Chrome on Windows"}',
--     '/dashboard/security',
--     'Voir les détails',
--     '/icons/security-alert.svg'
-- );

-- ==================== FIN DE LA MIGRATION ====================
