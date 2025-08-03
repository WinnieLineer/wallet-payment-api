package com.example.wallet.services

import com.example.wallet.models.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class DailyTransactionReport(
    val date: String,
    val transactions: List<TransactionRecord>,
    val summary: TransactionSummary
)

@Serializable
data class TransactionRecord(
    val transactionId: String,
    val walletId: String,
    val type: String,
    val amount: String,
    val currency: String,
    val status: String,
    val referenceId: String?,
    val timestamp: String
)

@Serializable
data class TransactionSummary(
    val totalTransactions: Int,
    val totalAmount: Map<String, String>, // Currency -> Amount
    val transactionsByType: Map<String, Int>,
    val transactionsByStatus: Map<String, Int>
)

@Serializable
data class ReconciliationResult(
    val date: String,
    val matched: List<TransactionRecord>,
    val missingInDb: List<TransactionRecord>,
    val missingInExternal: List<TransactionRecord>,
    val discrepancies: List<TransactionDiscrepancy>
)

@Serializable
data class TransactionDiscrepancy(
    val transactionId: String,
    val field: String,
    val dbValue: String,
    val externalValue: String
)

class ReconciliationService(private val walletService: WalletService) {
    
    fun generateDailyReport(date: LocalDate, format: String = "JSON"): String = transaction {
        val startOfDay = date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
        
        val transactions = (Transactions innerJoin Wallets).select {
            Transactions.createdAt.between(startOfDay, endOfDay) and 
            (Transactions.status eq TransactionStatus.COMPLETED)
        }.map { row ->
            TransactionRecord(
                transactionId = row[Transactions.id].toString(),
                walletId = row[Transactions.walletId].toString(),
                type = row[Transactions.type].toString(),
                amount = row[Transactions.amount].toString(),
                currency = row[Wallets.currency].toString(),
                status = row[Transactions.status].toString(),
                referenceId = row[Transactions.referenceId],
                timestamp = row[Transactions.createdAt].toString()
            )
        }
        
        val summary = calculateSummary(transactions)
        
        val report = DailyTransactionReport(
            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            transactions = transactions,
            summary = summary
        )
        
        return@transaction when (format.uppercase()) {
            "CSV" -> convertToCsv(report)
            else -> Json.encodeToString(report)
        }
    }
    
    fun reconcileTransactions(date: LocalDate, externalTransactions: List<TransactionRecord>): ReconciliationResult = transaction {
        val dbReport = Json.decodeFromString<DailyTransactionReport>(generateDailyReport(date))
        val dbTransactions = dbReport.transactions.associateBy { it.transactionId }
        val extTransactions = externalTransactions.associateBy { it.transactionId }
        
        val matched = mutableListOf<TransactionRecord>()
        val discrepancies = mutableListOf<TransactionDiscrepancy>()
        
        // Find matches and discrepancies
        dbTransactions.forEach { (id, dbTx) ->
            val extTx = extTransactions[id]
            if (extTx != null) {
                if (areTransactionsEqual(dbTx, extTx)) {
                    matched.add(dbTx)
                } else {
                    matched.add(dbTx)
                    discrepancies.addAll(findDiscrepancies(id, dbTx, extTx))
                }
            }
        }
        
        val missingInExternal = dbTransactions.keys.minus(extTransactions.keys).map { dbTransactions[it]!! }
        val missingInDb = extTransactions.keys.minus(dbTransactions.keys).map { extTransactions[it]!! }
        
        ReconciliationResult(
            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            matched = matched,
            missingInDb = missingInDb,
            missingInExternal = missingInExternal,
            discrepancies = discrepancies
        )
    }
    
    fun generateMockExternalFile(date: LocalDate): List<TransactionRecord> = transaction {
        val dbReport = Json.decodeFromString<DailyTransactionReport>(generateDailyReport(date))
        val mockTransactions = dbReport.transactions.toMutableList()
        
        // Simulate some reconciliation scenarios
        if (mockTransactions.isNotEmpty()) {
            // Remove one transaction to simulate missing in external
            if (mockTransactions.size > 1) {
                mockTransactions.removeAt(0)
            }
            
            // Modify one transaction to simulate discrepancy
            if (mockTransactions.isNotEmpty()) {
                val modified = mockTransactions[0].copy(
                    amount = (BigDecimal(mockTransactions[0].amount) + BigDecimal("10.00")).toString()
                )
                mockTransactions[0] = modified
            }
            
            // Add a phantom transaction
            mockTransactions.add(
                TransactionRecord(
                    transactionId = UUID.randomUUID().toString(),
                    walletId = UUID.randomUUID().toString(),
                    type = "PAYMENT",
                    amount = "50.00",
                    currency = "USD",
                    status = "COMPLETED",
                    referenceId = "PHANTOM_REF",
                    timestamp = date.atStartOfDay().toString()
                )
            )
        }
        
        mockTransactions
    }
    
    private fun calculateSummary(transactions: List<TransactionRecord>): TransactionSummary {
        val totalAmount = mutableMapOf<String, BigDecimal>()
        val transactionsByType = mutableMapOf<String, Int>()
        val transactionsByStatus = mutableMapOf<String, Int>()
        
        transactions.forEach { tx ->
            // Sum amounts by currency
            val amount = BigDecimal(tx.amount)
            totalAmount[tx.currency] = totalAmount.getOrDefault(tx.currency, BigDecimal.ZERO) + amount
            
            // Count by type
            transactionsByType[tx.type] = transactionsByType.getOrDefault(tx.type, 0) + 1
            
            // Count by status
            transactionsByStatus[tx.status] = transactionsByStatus.getOrDefault(tx.status, 0) + 1
        }
        
        return TransactionSummary(
            totalTransactions = transactions.size,
            totalAmount = totalAmount.mapValues { it.value.toString() },
            transactionsByType = transactionsByType,
            transactionsByStatus = transactionsByStatus
        )
    }
    
    private fun convertToCsv(report: DailyTransactionReport): String {
        val header = "TransactionId,WalletId,Type,Amount,Currency,Status,ReferenceId,Timestamp\n"
        val rows = report.transactions.joinToString("\n") { tx ->
            "${tx.transactionId},${tx.walletId},${tx.type},${tx.amount},${tx.currency},${tx.status},${tx.referenceId ?: ""},${tx.timestamp}"
        }
        return header + rows
    }
    
    private fun areTransactionsEqual(tx1: TransactionRecord, tx2: TransactionRecord): Boolean {
        return tx1.walletId == tx2.walletId &&
                tx1.type == tx2.type &&
                tx1.amount == tx2.amount &&
                tx1.currency == tx2.currency &&
                tx1.status == tx2.status
    }
    
    private fun findDiscrepancies(id: String, dbTx: TransactionRecord, extTx: TransactionRecord): List<TransactionDiscrepancy> {
        val discrepancies = mutableListOf<TransactionDiscrepancy>()
        
        if (dbTx.walletId != extTx.walletId) {
            discrepancies.add(TransactionDiscrepancy(id, "walletId", dbTx.walletId, extTx.walletId))
        }
        if (dbTx.type != extTx.type) {
            discrepancies.add(TransactionDiscrepancy(id, "type", dbTx.type, extTx.type))
        }
        if (dbTx.amount != extTx.amount) {
            discrepancies.add(TransactionDiscrepancy(id, "amount", dbTx.amount, extTx.amount))
        }
        if (dbTx.currency != extTx.currency) {
            discrepancies.add(TransactionDiscrepancy(id, "currency", dbTx.currency, extTx.currency))
        }
        if (dbTx.status != extTx.status) {
            discrepancies.add(TransactionDiscrepancy(id, "status", dbTx.status, extTx.status))
        }
        
        return discrepancies
    }
}