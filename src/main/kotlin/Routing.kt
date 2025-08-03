package com.example.wallet

import com.example.wallet.controllers.reconciliationRoutes
import com.example.wallet.controllers.transactionRoutes
import com.example.wallet.controllers.walletRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Wallet Payment API v1.0")
        }
        
        get("/health") {
            call.respondText("OK")
        }
        
        walletRoutes()
        transactionRoutes()
        reconciliationRoutes()
    }
}
