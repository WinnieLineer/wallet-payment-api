
# Wallet Payment API

A simple wallet management system providing APIs for user management, wallet operations, transactions (top‑up, payment, transfer), and reconciliation.

---

## Table of Contents
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [API Usage Examples](#-api-usage-examples)
- [Reconciliation](#-reconciliation)
- [Key Features](#-key-features)
- [Error Handling](#-error-handling)
- [Logging & Tracing](#-logging--tracing)
- [Development & Testing](#-development--testing)
- [Best Practices](#-best-practices)
- [Project Info](#project-info)

---

## 🚀 Quick Start

### Start the Application
```bash
# 1. Start PostgreSQL database
docker-compose up -d postgres

# 2. Run the application
./gradlew run
```
The application will start at `http://localhost:8080`.

---

## 📚 API Documentation
- **Swagger UI**: [http://localhost:8080/swagger](http://localhost:8080/swagger)  
- **API Docs**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)  
- **Health Check**: [http://localhost:8080/health](http://localhost:8080/health)

---

## 🏗️ API Usage Examples

### 1. Create a User
```bash
curl -X POST http://localhost:8080/api/users   -H "Content-Type: application/json"   -d '{
    "name": "Alice Chen"
  }'
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Alice Chen",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### 2. Create a Wallet
```bash
curl -X POST http://localhost:8080/api/wallets   -H "Content-Type: application/json"   -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "currency": "USD"
  }'
```

### 3. Get Wallet Balance
```bash
curl http://localhost:8080/api/wallets/{walletId}/balance
```

### 4. Top‑Up
```bash
curl -X POST http://localhost:8080/api/transactions/top-up   -H "Content-Type: application/json"   -d '{
    "walletId": "wallet-uuid-here",
    "amount": "100.50",
    "referenceId": "TOPUP-2024-001",
    "idempotencyKey": "unique-key-12345"
  }'
```

### 5. Payment
```bash
curl -X POST http://localhost:8080/api/transactions/payment   -H "Content-Type: application/json"   -d '{
    "walletId": "wallet-uuid-here",
    "amount": "25.00",
    "referenceId": "PAYMENT-2024-001",
    "idempotencyKey": "unique-key-67890"
  }'
```

### 6. Transfer
```bash
curl -X POST http://localhost:8080/api/transactions/transfer   -H "Content-Type: application/json"   -d '{
    "fromWalletId": "sender-wallet-uuid",
    "toWalletId": "receiver-wallet-uuid",
    "amount": "50.00",
    "referenceId": "TRANSFER-2024-001",
    "idempotencyKey": "unique-key-transfer-123"
  }'
```

### 7. Get Transaction History
```bash
# Transactions for a specific wallet
curl "http://localhost:8080/api/wallets/{walletId}/transactions?fromDate=2024-01-01&toDate=2024-01-31"

# Specific transaction
curl http://localhost:8080/api/transactions/{transactionId}
```

---

## 📊 Reconciliation

### Generate Daily Report
```bash
# JSON format
curl http://localhost:8080/api/reconciliation/report/2024-01-15

# CSV format
curl "http://localhost:8080/api/reconciliation/report/2024-01-15?format=CSV"
```

### Perform Reconciliation
```bash
curl -X POST http://localhost:8080/api/reconciliation/reconcile/2024-01-15   -H "Content-Type: application/json"   -d '[
    {
      "transactionId": "tx-uuid-1",
      "walletId": "wallet-uuid-1",
      "type": "TOP_UP",
      "amount": "100.00",
      "currency": "USD",
      "status": "COMPLETED",
      "referenceId": "REF-001",
      "timestamp": "2024-01-15T10:00:00Z"
    }
  ]'
```

### Generate Mock Reconciliation File
```bash
curl http://localhost:8080/api/reconciliation/mock-external/2024-01-15
```

---

## 🔐 Key Features

- **Idempotency**:  
  Each transaction request must include a unique `idempotencyKey`:
  - Same key → returns original result  
  - Prevents duplicate charges due to retries  
  - Ensures transaction uniqueness  

- **Supported Currencies**:  
  - **USD** – US Dollar  
  - **SGD** – Singapore Dollar  
  - **TWD** – New Taiwan Dollar  

- **Transaction Types**:  
  - `TOP_UP` – Top‑up  
  - `PAYMENT` – Payment  
  - `TRANSFER_OUT` – Transfer Out  
  - `TRANSFER_IN` – Transfer In  

- **Transaction Status**:  
  - `PENDING` – Processing  
  - `COMPLETED` – Completed  
  - `FAILED` – Failed  
  - `CANCELLED` – Cancelled  

---

## 🚨 Error Handling

### Common Error Response
```json
{
  "error": "Error message description"
}
```

### HTTP Status Codes
- `200`: Success  
- `201`: Resource Created  
- `400`: Invalid Request  
- `404`: Resource Not Found  
- `500`: Internal Server Error  

---

## 📝 Logging & Tracing

Each API request generates a unique `call-id`:
- Response header: `X-Request-ID`  
- Log file: `logs/wallet-api.log`  
- Logs include `call-id` for tracing  

---

## 🔧 Development & Testing

### Environment Configuration
```yaml
# application.yaml
db:
  url: "jdbc:postgresql://localhost:5432/walletdb"
  user: wallet_user
  password: password123
```

### Database Migrations
Handled automatically with Flyway:
- `V1__Init.sql`: Create base tables  
- `V2__Add_transaction_fields.sql`: Add transaction fields  

### Test Data
```bash
# Create test user and wallet
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name": "Test User"}'
curl -X POST http://localhost:8080/api/wallets -H "Content-Type: application/json" -d '{"userId": "user-id", "currency": "USD"}'
```

---

## 🎯 Best Practices
1. Always use `idempotencyKey` to prevent duplicate transactions.  
2. Check balance before processing payments.  
3. Record `referenceId` for tracking and reconciliation.  
4. Ensure currency consistency for transfers.  
5. Monitor logs regularly for errors or anomalies.  

---

## Project Info

This project was created using the **Ktor Project Generator**.

### Features
- **Koin** – Dependency injection  
- **Routing** – Structured routing DSL  
- **Content Negotiation** – Content conversion based on headers  
- **kotlinx.serialization** – JSON serialization  
- **Exposed** – Database ORM  
- **Call Logging** – Logs client requests  
- **Postgres** – PostgreSQL integration  
- **Swagger** – Interactive API documentation  

### Build & Run Tasks
- `./gradlew test` – Run tests  
- `./gradlew build` – Build project  
- `buildFatJar` – Build an executable JAR with dependencies  
- `buildImage` – Build Docker image  
- `publishImageToLocalRegistry` – Publish image locally  
- `run` – Run the server  
- `runDocker` – Run using Docker image  

**Sample successful startup log:**
```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

