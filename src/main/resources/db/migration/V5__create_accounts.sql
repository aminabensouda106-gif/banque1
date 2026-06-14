CREATE TYPE account_type AS ENUM ('COURANT', 'EPARGNE', 'PROFESSIONNEL');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'BLOCKED', 'CLOSED');

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

CREATE INDEX idx_accounts_client_id ON accounts(client_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
