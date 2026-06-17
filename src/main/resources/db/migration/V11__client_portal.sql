ALTER TABLE clients
    ADD COLUMN password_hash  VARCHAR(255),
    ADD COLUMN portal_enabled BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN last_login_at  TIMESTAMPTZ;

ALTER TABLE notifications
    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE notifications
    ADD COLUMN client_id BIGINT REFERENCES clients(id);

ALTER TABLE notifications
    ADD CONSTRAINT chk_notifications_recipient
        CHECK (
            (user_id IS NOT NULL AND client_id IS NULL)
            OR (user_id IS NULL AND client_id IS NOT NULL)
        );

CREATE INDEX idx_notifications_client_id ON notifications(client_id);
CREATE INDEX idx_notifications_client_read ON notifications(client_id, read);
