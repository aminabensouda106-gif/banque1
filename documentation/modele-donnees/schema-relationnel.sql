-- Schéma relationnel de référence — Banque Agence (PostgreSQL)
-- Ce script documente la conception (Phase 1).
-- L'implémentation Flyway reprendra cette structure en Phase 2+.

CREATE TYPE user_role AS ENUM ('ADMIN', 'AGENT', 'CHEF_AGENCE');
CREATE TYPE client_status AS ENUM ('ACTIVE', 'SUSPENDED', 'INACTIVE');
CREATE TYPE account_type AS ENUM ('COURANT', 'EPARGNE', 'PROFESSIONNEL');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'BLOCKED', 'CLOSED');
CREATE TYPE transaction_type AS ENUM ('DEPOT', 'RETRAIT', 'VIREMENT', 'PAIEMENT_FACTURE');
CREATE TYPE checkbook_order_status AS ENUM ('PENDING', 'PROCESSING', 'DELIVERED', 'CANCELLED');

-- Utilisateurs internes
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100),
    role            user_role    NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMPTZ
);

-- Clients de l'agence
CREATE TABLE clients (
    id                BIGSERIAL PRIMARY KEY,
    client_number     VARCHAR(20)  NOT NULL UNIQUE,
    cin               VARCHAR(20)  NOT NULL UNIQUE,
    first_name        VARCHAR(50)  NOT NULL,
    last_name         VARCHAR(50)  NOT NULL,
    email             VARCHAR(100),
    phone             VARCHAR(20),
    address           VARCHAR(255),
    professional_info TEXT,
    status            client_status NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Comptes bancaires
CREATE TABLE accounts (
    id              BIGSERIAL PRIMARY KEY,
    account_number  VARCHAR(20)    NOT NULL UNIQUE,
    client_id       BIGINT         NOT NULL REFERENCES clients(id),
    type            account_type   NOT NULL,
    status          account_status NOT NULL DEFAULT 'ACTIVE',
    balance         NUMERIC(19, 4) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    opened_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ,
    version         BIGINT         NOT NULL DEFAULT 0
);

-- Transactions financières
CREATE TABLE transactions (
    id                      BIGSERIAL PRIMARY KEY,
    type                    transaction_type NOT NULL,
    amount                  NUMERIC(19, 4)   NOT NULL CHECK (amount > 0),
    source_account_id       BIGINT REFERENCES accounts(id),
    destination_account_id  BIGINT REFERENCES accounts(id),
    executed_by             BIGINT         NOT NULL REFERENCES users(id),
    executed_at             TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    description             VARCHAR(255)
);

-- Journal d'audit
CREATE TABLE audit_logs (
    id            BIGSERIAL PRIMARY KEY,
    action        VARCHAR(50)  NOT NULL,
    entity_type   VARCHAR(50),
    entity_id     BIGINT,
    performed_by  BIGINT       NOT NULL REFERENCES users(id),
    performed_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    details       TEXT
);

-- Facturiers (catalogue — extension v1.1)
CREATE TABLE bill_providers (
    id        BIGSERIAL PRIMARY KEY,
    code      VARCHAR(20)  NOT NULL UNIQUE,
    name      VARCHAR(100) NOT NULL,
    category  VARCHAR(50),
    active    BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Paiements de facture (extension v1.1)
CREATE TABLE bill_payments (
    id                BIGSERIAL PRIMARY KEY,
    account_id        BIGINT         NOT NULL REFERENCES accounts(id),
    bill_provider_id  BIGINT         NOT NULL REFERENCES bill_providers(id),
    client_reference  VARCHAR(50)    NOT NULL,
    amount            NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    transaction_id    BIGINT         NOT NULL UNIQUE REFERENCES transactions(id),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- Commandes de chéquier (extension v1.1)
CREATE TABLE checkbook_orders (
    id            BIGSERIAL PRIMARY KEY,
    order_number  VARCHAR(20)            NOT NULL UNIQUE,
    account_id    BIGINT                 NOT NULL REFERENCES accounts(id),
    client_id     BIGINT                 NOT NULL REFERENCES clients(id),
    quantity      INTEGER                NOT NULL DEFAULT 1 CHECK (quantity > 0),
    status        checkbook_order_status NOT NULL DEFAULT 'PENDING',
    requested_at  TIMESTAMPTZ            NOT NULL DEFAULT NOW(),
    processed_at  TIMESTAMPTZ,
    delivered_at  TIMESTAMPTZ,
    requested_by  BIGINT                 NOT NULL REFERENCES users(id),
    notes         VARCHAR(255)
);

-- Index
CREATE INDEX idx_clients_cin ON clients(cin);
CREATE INDEX idx_clients_client_number ON clients(client_number);
CREATE INDEX idx_clients_name ON clients(last_name, first_name);
CREATE INDEX idx_accounts_client_id ON accounts(client_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_transactions_executed_at ON transactions(executed_at);
CREATE INDEX idx_transactions_source ON transactions(source_account_id);
CREATE INDEX idx_transactions_destination ON transactions(destination_account_id);
CREATE INDEX idx_audit_logs_performed_at ON audit_logs(performed_at);
CREATE INDEX idx_bill_payments_account_id ON bill_payments(account_id);
CREATE INDEX idx_bill_payments_transaction_id ON bill_payments(transaction_id);
CREATE INDEX idx_checkbook_orders_account_id ON checkbook_orders(account_id);
CREATE INDEX idx_checkbook_orders_status ON checkbook_orders(status);

-- Données de référence facturiers (exemples)
INSERT INTO bill_providers (code, name, category) VALUES
    ('LYDEC', 'LYDEC', 'Eau & électricité'),
    ('ONEE', 'ONEE', 'Électricité'),
    ('IAM', 'IAM', 'Télécom'),
    ('ORANGE', 'Orange Maroc', 'Télécom'),
    ('INWI', 'Inwi', 'Télécom');
