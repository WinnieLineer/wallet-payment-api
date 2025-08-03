package com.example.wallet.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureDocumentation() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/wallet-api.yaml") {
            version = "4.15.5"
        }
        
        get("/api-docs") {
            call.respondRedirect("/swagger")
        }
    }
}