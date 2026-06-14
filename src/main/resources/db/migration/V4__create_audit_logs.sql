CREATE TABLE audit_logs (
    id            BIGSERIAL PRIMARY KEY,
    action        VARCHAR(50)  NOT NULL,
    entity_type   VARCHAR(50),
    entity_id     BIGINT,
    performed_by  BIGINT       NOT NULL REFERENCES users(id),
    performed_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    details       TEXT
);

CREATE INDEX idx_audit_logs_performed_at ON audit_logs(performed_at);
