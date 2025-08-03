package com.example.wallet.services

import com.example.wallet.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class WalletService {
    private val logger = LoggerFactory.getLogger(WalletService::class.java)
    
    fun createUser(name: String): User = transaction {
        logger.info("Creating new user with name: $name")
        
        val userId = Users.insertAndGetId {
            it[Users.name] = name
            it[createdAt] = Instant.now()
        }.value
        
        logger.info("Successfully created user with ID: $userId")
        
        User(
            id = userId,
            name = name,
            createdAt = Instant.now()
        )
    }
    
    fun createWallet(userId: UUID, currency: Currency): Wallet = transaction {
        logger.info("Creating wallet for user: $userId with currency: $currency")
        
        // Check if user exists
        val userExists = Users.select { Users.id eq userId }.count() > 0
        if (!userExists) {
            logger.warn("Attempted to create wallet for non-existent user: $userId")
            throw IllegalArgumentException("User not found")
        }
        
        // Check if wallet already exists for this user and currency
        val existingWallet = Wallets.select { 
            (Wallets.userId eq userId) and (Wallets.currency eq currency) 
        }.singleOrNull()
        
        if (existingWallet != null) {
            logger.warn("Wallet already exists for user $userId with currency $currency")
            throw IllegalArgumentException("Wallet already exists for user $userId with currency $currency")
        }
        
        val walletId = Wallets.insertAndGetId {
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
            updatedAt = Instant.now()
        )
    }
    
    fun getWalletBalance(walletId: UUID): WalletBalanceResponse? = transaction {
        Wallets.select { Wallets.id eq walletId }
            .singleOrNull()
            ?.let { row ->
                WalletBalanceResponse(
                    walletId = row[Wallets.id],
                    currency = row[Wallets.currency],
                    balance = row[Wallets.balance]
                )
            }
    }
    
    fun getUserWallets(userId: UUID): List<Wallet> = transaction {
        Wallets.select { Wallets.userId eq userId }
            .map { row ->
                Wallet(
                    id = row[Wallets.id],
                    userId = row[Wallets.userId],
                    currency = row[Wallets.currency],
                    balance = row[Wallets.balance],
                    updatedAt = row[Wallets.updatedAt]
                )
            }
    }
    
    internal fun updateWalletBalance(walletId: UUID, newBalance: BigDecimal) = transaction {
        Wallets.update({ Wallets.id eq walletId }) {
            it[balance] = newBalance
            it[updatedAt] = Instant.now()
        }
    }
    
    internal fun getWallet(walletId: UUID): Wallet? = transaction {
        Wallets.select { Wallets.id eq walletId }
            .singleOrNull()
            ?.let { row ->
                Wallet(
                    id = row[Wallets.id],
                    userId = row[Wallets.userId],
                    currency = row[Wallets.currency],
                    balance = row[Wallets.balance],
                    updatedAt = row[Wallets.updatedAt]
                )
            }
    }
}