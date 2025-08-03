# Wallet Payment API - 使用指南

## 🚀 快速開始

### 啟動應用程式
```bash
# 1. 啟動 PostgreSQL 資料庫
docker-compose up -d postgres

# 2. 運行應用程式
./gradlew run
```

應用程式將在 `http://localhost:8080` 啟動

### 📚 API 文檔
- **Swagger UI**: http://localhost:8080/swagger
- **API Docs**: http://localhost:8080/api-docs
- **健康檢查**: http://localhost:8080/health

## 🏗️ API 使用範例

### 1. 創建用戶
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Chen"
  }'
```

**回應:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Alice Chen",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### 2. 創建錢包
```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "currency": "USD"
  }'
```

### 3. 查詢餘額
```bash
curl http://localhost:8080/api/wallets/{walletId}/balance
```

### 4. 充值 (Top-up)
```bash
curl -X POST http://localhost:8080/api/transactions/top-up \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "wallet-uuid-here",
    "amount": "100.50",
    "referenceId": "TOPUP-2024-001",
    "idempotencyKey": "unique-key-12345"
  }'
```

### 5. 扣款 (Payment)
```bash
curl -X POST http://localhost:8080/api/transactions/payment \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "wallet-uuid-here",
    "amount": "25.00",
    "referenceId": "PAYMENT-2024-001",
    "idempotencyKey": "unique-key-67890"
  }'
```

### 6. 轉帳 (Transfer)
```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "fromWalletId": "sender-wallet-uuid",
    "toWalletId": "receiver-wallet-uuid",
    "amount": "50.00",
    "referenceId": "TRANSFER-2024-001",
    "idempotencyKey": "unique-key-transfer-123"
  }'
```

### 7. 查詢交易記錄
```bash
# 查詢特定錢包的交易記錄
curl "http://localhost:8080/api/wallets/{walletId}/transactions?fromDate=2024-01-01&toDate=2024-01-31"

# 查詢特定交易狀態
curl http://localhost:8080/api/transactions/{transactionId}
```

## 📊 對帳功能

### 生成每日報表
```bash
# JSON 格式
curl http://localhost:8080/api/reconciliation/report/2024-01-15

# CSV 格式
curl "http://localhost:8080/api/reconciliation/report/2024-01-15?format=CSV"
```

### 執行對帳
```bash
curl -X POST http://localhost:8080/api/reconciliation/reconcile/2024-01-15 \
  -H "Content-Type: application/json" \
  -d '[
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

### 生成模擬對帳檔案
```bash
curl http://localhost:8080/api/reconciliation/mock-external/2024-01-15
```

## 🔐 重要特性

### 防重複交易 (Idempotency)
每個交易請求都必須包含唯一的 `idempotencyKey`：
- 相同的 key 重複請求會返回原始交易結果
- 防止網路重試造成的重複扣款
- 確保交易的唯一性

### 支援幣別
- **USD**: 美元
- **SGD**: 新加坡幣  
- **TWD**: 新台幣

### 交易類型
- **TOP_UP**: 充值
- **PAYMENT**: 扣款
- **TRANSFER_OUT**: 轉出
- **TRANSFER_IN**: 轉入

### 交易狀態
- **PENDING**: 處理中
- **COMPLETED**: 已完成
- **FAILED**: 失敗
- **CANCELLED**: 已取消

## 🚨 錯誤處理

### 常見錯誤回應
```json
{
  "error": "錯誤訊息描述"
}
```

### HTTP 狀態碼
- `200`: 成功
- `201`: 資源創建成功
- `400`: 請求無效
- `404`: 資源不存在
- `500`: 伺服器內部錯誤

## 📝 日誌追蹤

每個 API 請求都會產生唯一的 `call-id`，可用於日誌追蹤：
- 在回應標頭中查找 `X-Request-ID`
- 日誌檔案位置: `logs/wallet-api.log`
- 日誌格式包含 call-id 方便追蹤

## 🔧 開發與測試

### 環境設定
```yaml
# application.yaml
db:
  url: "jdbc:postgresql://localhost:5432/walletdb"
  user: wallet_user
  password: password123
```

### 資料庫遷移
Flyway 會自動執行資料庫遷移：
- `V1__Init.sql`: 建立基本表格
- `V2__Add_transaction_fields.sql`: 新增交易相關欄位

### 測試資料
```bash
# 創建測試用戶和錢包
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name": "Test User"}'
curl -X POST http://localhost:8080/api/wallets -H "Content-Type: application/json" -d '{"userId": "user-id", "currency": "USD"}'
```

## 🎯 最佳實務

1. **總是使用 idempotencyKey** 避免重複交易
2. **檢查餘額** 在扣款前確認餘額充足
3. **記錄 referenceId** 便於追蹤和對帳
4. **使用正確的幣別** 轉帳時確保幣別一致
5. **監控日誌** 定期檢查錯誤和異常情況