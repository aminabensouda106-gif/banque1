ALTER TYPE transaction_type ADD VALUE 'PAIEMENT_FACTURE';

CREATE TABLE bill_providers (
    id        BIGSERIAL PRIMARY KEY,
    code      VARCHAR(20)  NOT NULL UNIQUE,
    name      VARCHAR(100) NOT NULL,
    category  VARCHAR(50),
    active    BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE bill_payments (
    id                BIGSERIAL PRIMARY KEY,
    account_id        BIGINT         NOT NULL REFERENCES accounts(id),
    bill_provider_id  BIGINT         NOT NULL REFERENCES bill_providers(id),
    client_reference  VARCHAR(50)    NOT NULL,
    amount            NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    transaction_id    BIGINT         NOT NULL UNIQUE REFERENCES transactions(id),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bill_payments_account_id ON bill_payments(account_id);
CREATE INDEX idx_bill_payments_transaction_id ON bill_payments(transaction_id);

INSERT INTO bill_providers (code, name, category) VALUES
    ('LYDEC', 'LYDEC', 'Eau & électricité'),
    ('ONEE', 'ONEE', 'Électricité'),
    ('IAM', 'IAM', 'Télécom'),
    ('ORANGE', 'Orange Maroc', 'Télécom'),
    ('INWI', 'Inwi', 'Télécom');
