package com.example.wallet.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class TransactionType {
    TOP_UP,
    PAYMENT,
    TRANSFER_OUT,
    TRANSFER_IN,
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
}

object Transactions : UUIDTable("transactions") {
    val walletId = uuid("wallet_id").references(Wallets.id)
    val type = enumeration<TransactionType>("type")
    val amount = decimal("amount", 18, 2)
    val referenceId = varchar("reference_id", 100).nullable()
    val status = enumeration<TransactionStatus>("status").default(TransactionStatus.PENDING)
    val targetWalletId = uuid("target_wallet_id").nullable()
    val idempotencyKey = varchar("idempotency_key", 100).uniqueIndex().nullable()
    val createdAt = timestamp("created_at")
}

@Serializable
data class Transaction(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val walletId: UUID,
    val type: TransactionType,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val referenceId: String?,
    val status: TransactionStatus,
    @Serializable(with = UUIDSerializer::class)
    val targetWalletId: UUID?,
    val idempotencyKey: String?,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
)

@Serializable
data class TopUpRequest(
    @Serializable(with = UUIDSerializer::class)
    val walletId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val referenceId: String?,
    val idempotencyKey: String,
)

@Serializable
data class PaymentRequest(
    @Serializable(with = UUIDSerializer::class)
    val walletId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val referenceId: String?,
    val idempotencyKey: String,
)

@Serializable
data class TransferRequest(
    @Serializable(with = UUIDSerializer::class)
    val fromWalletId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val toWalletId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val referenceId: String?,
    val idempotencyKey: String,
)

@Serializable
data class TransactionResponse(
    @Serializable(with = UUIDSerializer::class)
    val transactionId: UUID,
    val status: TransactionStatus,
    val message: String,
)
