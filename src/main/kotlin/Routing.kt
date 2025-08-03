package com.example.wallet

import com.example.wallet.controllers.reconciliationRoutes
import com.example.wallet.controllers.transactionRoutes
import com.example.wallet.controllers.walletRoutes
import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/openapi.yaml") {
            version = "4.15.5"
        }

        get("/health") {
            call.respondText("OK")
        }

        walletRoutes()
        transactionRoutes()
        reconciliationRoutes()
    }
}
