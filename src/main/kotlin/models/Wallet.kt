package com.example.wallet.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.java.time.timestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.*

enum class Currency {
    USD, SGD, TWD
}

object Wallets : Table() {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(Users.id)
    val currency = enumeration<Currency>("currency")
    val balance = decimal("balance", 18, 2).default(BigDecimal.ZERO)
    val updatedAt = timestamp("updated_at")
    
    override val primaryKey = PrimaryKey(id)
}

data class Wallet(
    val id: UUID,
    val userId: UUID,
    val currency: Currency,
    val balance: BigDecimal,
    val updatedAt: Instant
)

data class CreateWalletRequest(
    val userId: UUID,
    val currency: Currency
)

data class WalletBalanceResponse(
    val walletId: UUID,
    val currency: Currency,
    val balance: BigDecimal
)