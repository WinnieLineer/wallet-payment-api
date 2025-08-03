package com.example.wallet.controllers

import com.example.wallet.services.ReconciliationService
import com.example.wallet.services.TransactionRecord
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
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
                            "attachment; filename=\"transactions_${date}.csv\""
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