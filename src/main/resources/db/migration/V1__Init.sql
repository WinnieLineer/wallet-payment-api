CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallets (
                         id UUID PRIMARY KEY,
                         user_id UUID REFERENCES users(id),
                         currency VARCHAR(3) NOT NULL,
                         balance DECIMAL(18,2) DEFAULT 0,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
                              id UUID PRIMARY KEY,
                              wallet_id UUID REFERENCES wallets(id),
                              type VARCHAR(20) NOT NULL,
                              amount DECIMAL(18,2) NOT NULL,
                              reference_id VARCHAR(100),
                              status VARCHAR(20) DEFAULT 'PENDING',
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
