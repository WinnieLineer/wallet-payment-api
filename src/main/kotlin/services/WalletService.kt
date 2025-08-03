package com.example.wallet.services

import com.example.wallet.models.Currency
import com.example.wallet.models.User
import com.example.wallet.models.Users
import com.example.wallet.models.Wallet
import com.example.wallet.models.WalletBalanceResponse
import com.example.wallet.models.Wallets
import com.example.wallet.models.Wallets.balance
import com.example.wallet.models.Wallets.updatedAt
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class WalletService {
    private val logger = LoggerFactory.getLogger(WalletService::class.java)

    fun createUser(name: String): User =
        transaction {
            logger.info("Creating new user with name: $name")

            val userId =
                Users.insertAndGetId {
                    it[Users.name] = name
                    it[createdAt] = Instant.now()
                }.value

            logger.info("Successfully created user with ID: $userId")

            User(
                id = userId,
                name = name,
                createdAt = Instant.now(),
            )
        }

    fun createWallet(
        userId: UUID,
        currency: Currency,
    ): Wallet =
        transaction {
            logger.info("Creating wallet for user: $userId with currency: $currency")

            // Check if user exists
            val userExists =
                Users.select(Users.id)
                    .where {
                        (Users.id eq userId)
                    }
                    .count() > 0

            if (!userExists) {
                logger.warn("Attempted to create wallet for non-existent user: $userId")
                throw IllegalArgumentException("User not found")
            }

            val existingWallet =
                Wallets.selectAll()
                    .where {
                        (Wallets.userId eq userId) and (Wallets.currency eq currency)
                    }
                    .singleOrNull()

            if (existingWallet != null) {
                logger.warn("Wallet already exists for user $userId with currency $currency")
                throw IllegalArgumentException("Wallet already exists for user $userId with currency $currency")
            }

            val walletId =
                Wallets.insertAndGetId {
                    it[Wallets.userId] = userId
                    it[Wallets.currency] = currency
                    it[balance] = BigDecimal.ZERO
                    it[updatedAt] = Instant.now()
                }.value

            logger.info("Successfully created wallet with ID: $walletId for user: $userId")

            Wallet(
                id = walletId,
                userId = userId,
                currency = currency,
                balance = BigDecimal.ZERO,
                updatedAt = Instant.now(),
            )
        }

    fun getWalletBalance(walletId: UUID): WalletBalanceResponse? =
        transaction {
            Wallets.selectAll().where { Wallets.id eq walletId }
                .singleOrNull()
                ?.let { row ->
                    WalletBalanceResponse(
                        walletId = row[Wallets.id].value,
                        currency = row[Wallets.currency],
                        balance = row[balance],
                    )
                }
        }

    fun getUserWallets(userId: UUID): List<Wallet> =
        transaction {
            Wallets.selectAll().where { Wallets.userId eq userId }
                .map { row ->
                    Wallet(
                        id = row[Wallets.id].value,
                        userId = row[Wallets.userId],
                        currency = row[Wallets.currency],
                        balance = row[balance],
                        updatedAt = row[updatedAt],
                    )
                }
        }

    internal fun updateWalletBalance(
        walletId: UUID,
        newBalance: BigDecimal,
    ) = transaction {
        Wallets.update({ Wallets.id eq walletId }) {
            it[balance] = newBalance
            it[updatedAt] = Instant.now()
        }
    }

    internal fun getWallet(walletId: UUID): Wallet? =
        transaction {
            Wallets.selectAll().where { Wallets.id eq walletId }
                .singleOrNull()
                ?.let { row ->
                    Wallet(
                        id = row[Wallets.id].value,
                        userId = row[Wallets.userId],
                        currency = row[Wallets.currency],
                        balance = row[balance],
                        updatedAt = row[updatedAt],
                    )
                }
        }
}
