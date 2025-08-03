package com.example.wallet.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.java.time.timestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.*

enum class TransactionType {
    TOP_UP, PAYMENT, TRANSFER_OUT, TRANSFER_IN
}

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED, CANCELLED
}

object Transactions : Table() {
    val id = uuid("id").autoGenerate()
    val walletId = uuid("wallet_id").references(Wallets.id)
    val type = enumeration<TransactionType>("type")
    val amount = decimal("amount", 18, 2)
    val referenceId = varchar("reference_id", 100).nullable()
    val status = enumeration<TransactionStatus>("status").default(TransactionStatus.PENDING)
    val targetWalletId = uuid("target_wallet_id").nullable() // For transfers
    val idempotencyKey = varchar("idempotency_key", 100).uniqueIndex().nullable()
    val createdAt = timestamp("created_at")
    
    override val primaryKey = PrimaryKey(id)
}

data class Transaction(
    val id: UUID,
    val walletId: UUID,
    val type: TransactionType,
    val amount: BigDecimal,
    val referenceId: String?,
    val status: TransactionStatus,
    val targetWalletId: UUID?,
    val idempotencyKey: String?,
    val createdAt: Instant
)

data class TopUpRequest(
    val walletId: UUID,
    val amount: BigDecimal,
    val referenceId: String?,
    val idempotencyKey: String
)

data class PaymentRequest(
    val walletId: UUID,
    val amount: BigDecimal,
    val referenceId: String?,
    val idempotencyKey: String
)

data class TransferRequest(
    val fromWalletId: UUID,
    val toWalletId: UUID,
    val amount: BigDecimal,
    val referenceId: String?,
    val idempotencyKey: String
)

data class TransactionResponse(
    val transactionId: UUID,
    val status: TransactionStatus,
    val message: String
)