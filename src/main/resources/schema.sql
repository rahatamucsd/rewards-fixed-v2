CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   VARCHAR(10)     PRIMARY KEY,
    customer_id      VARCHAR(10)     NOT NULL,
    customer_name    VARCHAR(100)    NOT NULL,
    transaction_date DATE            NOT NULL,
    amount           DECIMAL(10, 2)  NOT NULL
);
