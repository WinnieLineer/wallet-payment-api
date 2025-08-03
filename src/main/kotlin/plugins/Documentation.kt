package com.example.wallet.plugins

import io.ktor.server.application.Application
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

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
