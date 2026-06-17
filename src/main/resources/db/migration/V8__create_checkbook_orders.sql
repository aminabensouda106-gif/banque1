CREATE TYPE checkbook_order_status AS ENUM ('PENDING', 'PROCESSING', 'DELIVERED', 'CANCELLED');

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

CREATE INDEX idx_checkbook_orders_account_id ON checkbook_orders(account_id);
CREATE INDEX idx_checkbook_orders_status ON checkbook_orders(status);
CREATE INDEX idx_checkbook_orders_client_id ON checkbook_orders(client_id);
