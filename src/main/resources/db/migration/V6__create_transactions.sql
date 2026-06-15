CREATE TYPE transaction_type AS ENUM ('DEPOT', 'RETRAIT', 'VIREMENT');

CREATE TABLE transactions (
    id                      BIGSERIAL PRIMARY KEY,
    type                    transaction_type NOT NULL,
    amount                  NUMERIC(19, 4)   NOT NULL CHECK (amount > 0),
    source_account_id       BIGINT REFERENCES accounts(id),
    destination_account_id  BIGINT REFERENCES accounts(id),
    executed_by             BIGINT           NOT NULL REFERENCES users(id),
    executed_at             TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    description             VARCHAR(255)
);

CREATE INDEX idx_transactions_executed_at ON transactions(executed_at);
CREATE INDEX idx_transactions_source ON transactions(source_account_id);
CREATE INDEX idx_transactions_destination ON transactions(destination_account_id);
