package com.example.wallet.services

import com.example.wallet.models.PaymentRequest
import com.example.wallet.models.TopUpRequest
import com.example.wallet.models.Transaction
import com.example.wallet.models.TransactionResponse
import com.example.wallet.models.TransactionStatus
import com.example.wallet.models.TransactionType
import com.example.wallet.models.Transactions
import com.example.wallet.models.TransferRequest
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class TransactionService(private val walletService: WalletService) {
    fun topUp(request: TopUpRequest): TransactionResponse =
        transaction {
            // Check for duplicate transaction using idempotency key
            val existingTransaction = findTransactionByIdempotencyKey(request.idempotencyKey)
            if (existingTransaction != null) {
                return@transaction TransactionResponse(
                    transactionId = existingTransaction.id,
                    status = existingTransaction.status,
                    message = "Transaction already processed",
                )
            }

            // Validate wallet exists
            val wallet =
                walletService.getWallet(request.walletId)
                    ?: throw IllegalArgumentException("Wallet not found")

            // Validate amount
            if (request.amount <= BigDecimal.ZERO) {
                throw IllegalArgumentException("Amount must be positive")
            }

            // Create transaction
            val transactionId =
                Transactions.insertAndGetId {
                    it[walletId] = request.walletId
                    it[type] = TransactionType.TOP_UP
                    it[amount] = request.amount
                    it[referenceId] = request.referenceId
                    it[status] = TransactionStatus.PENDING
                    it[idempotencyKey] = request.idempotencyKey
                    it[createdAt] = Instant.now()
                }.value

            try {
                // Update wallet balance
                val newBalance = wallet.balance + request.amount
                walletService.updateWalletBalance(request.walletId, newBalance)

                // Mark transaction as completed
                updateTransactionStatus(transactionId, TransactionStatus.COMPLETED)

                TransactionResponse(
                    transactionId = transactionId,
                    status = TransactionStatus.COMPLETED,
                    message = "Top-up successful",
                )
            } catch (e: Exception) {
                // Mark transaction as failed
                updateTransactionStatus(transactionId, TransactionStatus.FAILED)
                throw e
            }
        }

    fun payment(request: PaymentRequest): TransactionResponse =
        transaction {
            // Check for duplicate transaction
            val existingTransaction = findTransactionByIdempotencyKey(request.idempotencyKey)
            if (existingTransaction != null) {
                return@transaction TransactionResponse(
                    transactionId = existingTransaction.id,
                    status = existingTransaction.status,
                    message = "Transaction already processed",
                )
            }

            // Validate wallet and balance
            val wallet =
                walletService.getWallet(request.walletId)
                    ?: throw IllegalArgumentException("Wallet not found")

            if (request.amount <= BigDecimal.ZERO) {
                throw IllegalArgumentException("Amount must be positive")
            }

            if (wallet.balance < request.amount) {
                throw IllegalArgumentException("Insufficient balance")
            }

            // Create transaction
            val transactionId =
                Transactions.insertAndGetId {
                    it[walletId] = request.walletId
                    it[type] = TransactionType.PAYMENT
                    it[amount] = request.amount
                    it[referenceId] = request.referenceId
                    it[status] = TransactionStatus.PENDING
                    it[idempotencyKey] = request.idempotencyKey
                    it[createdAt] = Instant.now()
                }.value

            try {
                // Update wallet balance
                val newBalance = wallet.balance - request.amount
                walletService.updateWalletBalance(request.walletId, newBalance)

                // Mark transaction as completed
                updateTransactionStatus(transactionId, TransactionStatus.COMPLETED)

                TransactionResponse(
                    transactionId = transactionId,
                    status = TransactionStatus.COMPLETED,
                    message = "Payment successful",
                )
            } catch (e: Exception) {
                updateTransactionStatus(transactionId, TransactionStatus.FAILED)
                throw e
            }
        }

    fun transfer(request: TransferRequest): TransactionResponse =
        transaction {
            // Check for duplicate transaction
            val existingTransaction = findTransactionByIdempotencyKey(request.idempotencyKey)
            if (existingTransaction != null) {
                return@transaction TransactionResponse(
                    transactionId = existingTransaction.id,
                    status = existingTransaction.status,
                    message = "Transaction already processed",
                )
            }

            // Validate wallets
            val fromWallet =
                walletService.getWallet(request.fromWalletId)
                    ?: throw IllegalArgumentException("Source wallet not found")
            val toWallet =
                walletService.getWallet(request.toWalletId)
                    ?: throw IllegalArgumentException("Target wallet not found")

            // Validate same currency
            if (fromWallet.currency != toWallet.currency) {
                throw IllegalArgumentException("Cannot transfer between different currencies")
            }

            // Validate amount and balance
            if (request.amount <= BigDecimal.ZERO) {
                throw IllegalArgumentException("Amount must be positive")
            }

            if (fromWallet.balance < request.amount) {
                throw IllegalArgumentException("Insufficient balance")
            }

            // Create outbound transaction
            val outTransactionId =
                Transactions.insertAndGetId {
                    it[walletId] = request.fromWalletId
                    it[type] = TransactionType.TRANSFER_OUT
                    it[amount] = request.amount
                    it[referenceId] = request.referenceId
                    it[status] = TransactionStatus.PENDING
                    it[targetWalletId] = request.toWalletId
                    it[idempotencyKey] = request.idempotencyKey
                    it[createdAt] = Instant.now()
                }.value

            // Create inbound transaction
            val inTransactionId =
                Transactions.insertAndGetId {
                    it[walletId] = request.toWalletId
                    it[type] = TransactionType.TRANSFER_IN
                    it[amount] = request.amount
                    it[referenceId] = request.referenceId
                    it[status] = TransactionStatus.PENDING
                    it[targetWalletId] = request.fromWalletId
                    it[idempotencyKey] = "${request.idempotencyKey}_in"
                    it[createdAt] = Instant.now()
                }.value

            try {
                // Update balances
                walletService.updateWalletBalance(request.fromWalletId, fromWallet.balance - request.amount)
                walletService.updateWalletBalance(request.toWalletId, toWallet.balance + request.amount)

                // Mark transactions as completed
                updateTransactionStatus(outTransactionId, TransactionStatus.COMPLETED)
                updateTransactionStatus(inTransactionId, TransactionStatus.COMPLETED)

                TransactionResponse(
                    transactionId = outTransactionId,
                    status = TransactionStatus.COMPLETED,
                    message = "Transfer successful",
                )
            } catch (e: Exception) {
                updateTransactionStatus(outTransactionId, TransactionStatus.FAILED)
                updateTransactionStatus(inTransactionId, TransactionStatus.FAILED)
                throw e
            }
        }

    fun getTransactionsByWallet(
        walletId: UUID,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): List<Transaction> =
        transaction {
            var query = Transactions.select { Transactions.walletId eq walletId }

            fromDate?.let {
                query =
                    query.andWhere {
                        Transactions.createdAt greaterEq
                            it.atStartOfDay()
                                .toInstant(java.time.ZoneOffset.UTC)
                    }
            }
            toDate?.let {
                query =
                    query.andWhere {
                        Transactions.createdAt lessEq it.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
                    }
            }

            query.orderBy(Transactions.createdAt, SortOrder.DESC)
                .map { row ->
                    Transaction(
                        id = row[Transactions.id].value,
                        walletId = row[Transactions.walletId],
                        type = TransactionType.valueOf(row[Transactions.type].toString()),
                        amount = row[Transactions.amount],
                        referenceId = row[Transactions.referenceId],
                        status = TransactionStatus.valueOf(row[Transactions.status].toString()),
                        targetWalletId = row[Transactions.targetWalletId],
                        idempotencyKey = row[Transactions.idempotencyKey],
                        createdAt = row[Transactions.createdAt],
                    )
                }
        }

    fun getTransactionStatus(transactionId: UUID): Transaction? =
        transaction {
            Transactions.select { Transactions.id eq transactionId }
                .singleOrNull()
                ?.let { row ->
                    Transaction(
                        id = row[Transactions.id].value,
                        walletId = row[Transactions.walletId],
                        type = TransactionType.valueOf(row[Transactions.type].toString()),
                        amount = row[Transactions.amount],
                        referenceId = row[Transactions.referenceId],
                        status = TransactionStatus.valueOf(row[Transactions.status].toString()),
                        targetWalletId = row[Transactions.targetWalletId],
                        idempotencyKey = row[Transactions.idempotencyKey],
                        createdAt = row[Transactions.createdAt],
                    )
                }
        }

    private fun findTransactionByIdempotencyKey(key: String): Transaction? =
        transaction {
            Transactions.select { Transactions.idempotencyKey eq key }
                .singleOrNull()
                ?.let { row ->
                    Transaction(
                        id = row[Transactions.id].value,
                        walletId = row[Transactions.walletId],
                        type = TransactionType.valueOf(row[Transactions.type].toString()),
                        amount = row[Transactions.amount],
                        referenceId = row[Transactions.referenceId],
                        status = TransactionStatus.valueOf(row[Transactions.status].toString()),
                        targetWalletId = row[Transactions.targetWalletId],
                        idempotencyKey = row[Transactions.idempotencyKey],
                        createdAt = row[Transactions.createdAt],
                    )
                }
        }

    private fun updateTransactionStatus(
        transactionId: UUID,
        status: TransactionStatus,
    ) {
        Transactions.update({ Transactions.id eq transactionId }) {
            it[Transactions.status] = status
        }
    }
}
