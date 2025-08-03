package com.example.wallet.controllers

import com.example.wallet.models.*
import com.example.wallet.services.TransactionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.util.*

fun Route.transactionRoutes() {
    val transactionService by inject<TransactionService>()
    
    route("/api/transactions") {
        post("/top-up") {
            try {
                val request = call.receive<TopUpRequest>()
                val response = transactionService.topUp(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        post("/payment") {
            try {
                val request = call.receive<PaymentRequest>()
                val response = transactionService.payment(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        post("/transfer") {
            try {
                val request = call.receive<TransferRequest>()
                val response = transactionService.transfer(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        get("/{transactionId}") {
            try {
                val transactionId = UUID.fromString(call.parameters["transactionId"])
                val transaction = transactionService.getTransactionStatus(transactionId)
                if (transaction != null) {
                    call.respond(HttpStatusCode.OK, transaction)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Transaction not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid transaction ID"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
    
    route("/api/wallets/{walletId}/transactions") {
        get {
            try {
                val walletId = UUID.fromString(call.parameters["walletId"])
                val fromDate = call.request.queryParameters["fromDate"]?.let { LocalDate.parse(it) }
                val toDate = call.request.queryParameters["toDate"]?.let { LocalDate.parse(it) }
                
                val transactions = transactionService.getTransactionsByWallet(walletId, fromDate, toDate)
                call.respond(HttpStatusCode.OK, transactions)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid parameters"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}