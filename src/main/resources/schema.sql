CREATE TABLE IF NOT EXISTS customers (
    customer_id   VARCHAR(10)  PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   VARCHAR(10)    PRIMARY KEY,
    customer_id      VARCHAR(10)    NOT NULL,
    transaction_date DATE           NOT NULL,
    amount           DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);
