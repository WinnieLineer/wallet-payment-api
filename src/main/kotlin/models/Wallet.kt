package com.example.wallet.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class Currency {
    USD,
    SGD,
    TWD,
}

object Wallets : UUIDTable() {
    val userId = uuid("user_id").references(Users.id)
    val currency = enumerationByName("currency", 10, Currency::class) // 10 是 varchar 長度
    val balance = decimal("balance", 18, 2).default(BigDecimal.ZERO)
    val updatedAt = timestamp("updated_at")
}

@Serializable
data class Wallet(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val currency: Currency,
    @Serializable(with = BigDecimalSerializer::class)
    val balance: BigDecimal,
    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant,
)

@Serializable
data class CreateWalletRequest(
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val currency: Currency,
)

@Serializable
data class WalletBalanceResponse(
    @Serializable(with = UUIDSerializer::class)
    val walletId: UUID,
    val currency: Currency,
    @Serializable(with = BigDecimalSerializer::class)
    val balance: BigDecimal,
)
