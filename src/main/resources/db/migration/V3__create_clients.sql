CREATE TYPE client_status AS ENUM ('ACTIVE', 'SUSPENDED', 'INACTIVE');

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

CREATE INDEX idx_clients_cin ON clients(cin);
CREATE INDEX idx_clients_client_number ON clients(client_number);
CREATE INDEX idx_clients_name ON clients(last_name, first_name);
