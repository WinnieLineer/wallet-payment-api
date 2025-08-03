-- Add missing columns for idempotency and transfers
ALTER TABLE transactions ADD COLUMN target_wallet_id UUID REFERENCES wallets(id);
ALTER TABLE transactions ADD COLUMN idempotency_key VARCHAR(100);

-- Create unique index for idempotency key
CREATE UNIQUE INDEX idx_transactions_idempotency_key ON transactions(idempotency_key) WHERE idempotency_key IS NOT NULL;

-- Update transaction type enum to support new types
ALTER TABLE transactions ALTER COLUMN type TYPE VARCHAR(20);

-- Update status enum to support new statuses  
ALTER TABLE transactions ALTER COLUMN status TYPE VARCHAR(20);