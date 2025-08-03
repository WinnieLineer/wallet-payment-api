package com.example.wallet.controllers

import com.example.wallet.models.PaymentRequest
import com.example.wallet.models.TopUpRequest
import com.example.wallet.models.TransferRequest
import com.example.wallet.services.TransactionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.util.UUID

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
