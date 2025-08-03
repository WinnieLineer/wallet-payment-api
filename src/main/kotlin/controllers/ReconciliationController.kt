package com.example.wallet.controllers

import com.example.wallet.services.ReconciliationService
import com.example.wallet.services.TransactionRecord
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.time.LocalDate

fun Route.reconciliationRoutes() {
    val reconciliationService by inject<ReconciliationService>()

    route("/api/reconciliation") {
        get("/report/{date}") {
            try {
                val date = LocalDate.parse(call.parameters["date"])
                val format = call.request.queryParameters["format"] ?: "JSON"

                val report = reconciliationService.generateDailyReport(date, format)

                when (format.uppercase()) {
                    "CSV" -> {
                        call.response.header(
                            HttpHeaders.ContentDisposition,
                            "attachment; filename=\"transactions_$date.csv\"",
                        )
                        call.respondText(report, ContentType.Text.CSV)
                    }
                    else -> {
                        call.respondText(report, ContentType.Application.Json)
                    }
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        post("/reconcile/{date}") {
            try {
                val date = LocalDate.parse(call.parameters["date"])
                val externalTransactions = call.receive<List<TransactionRecord>>()

                val result = reconciliationService.reconcileTransactions(date, externalTransactions)
                call.respond(HttpStatusCode.OK, result)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        get("/mock-external/{date}") {
            try {
                val date = LocalDate.parse(call.parameters["date"])
                val mockData = reconciliationService.generateMockExternalFile(date)
                call.respond(HttpStatusCode.OK, mockData)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}
