package com.example.wallet.controllers

import com.example.wallet.models.*
import com.example.wallet.services.WalletService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.walletRoutes() {
    val walletService by inject<WalletService>()
    
    route("/api/users") {
        post {
            try {
                val request = call.receive<CreateUserRequest>()
                val user = walletService.createUser(request.name)
                call.respond(HttpStatusCode.Created, user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        get("/{userId}/wallets") {
            try {
                val userId = UUID.fromString(call.parameters["userId"])
                val wallets = walletService.getUserWallets(userId)
                call.respond(HttpStatusCode.OK, wallets)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
    
    route("/api/wallets") {
        post {
            try {
                val request = call.receive<CreateWalletRequest>()
                val wallet = walletService.createWallet(request.userId, request.currency)
                call.respond(HttpStatusCode.Created, wallet)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        get("/{walletId}/balance") {
            try {
                val walletId = UUID.fromString(call.parameters["walletId"])
                val balance = walletService.getWalletBalance(walletId)
                if (balance != null) {
                    call.respond(HttpStatusCode.OK, balance)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Wallet not found"))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid wallet ID"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}