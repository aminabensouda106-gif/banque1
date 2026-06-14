-- Baseline Phase 2 — le schéma complet sera ajouté en Phase 3
CREATE TABLE IF NOT EXISTS flyway_baseline (
    id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1)
);

INSERT INTO flyway_baseline (id) VALUES (1) ON CONFLICT DO NOTHING;
