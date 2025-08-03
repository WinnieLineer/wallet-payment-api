package com.example.wallet

import com.example.wallet.plugins.configureDocumentation
import com.example.wallet.plugins.configureLogging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val dbConfig = environment.config.config("db")
    val dbUrl = dbConfig.property("url").getString()
    val dbUser = dbConfig.property("user").getString()
    val dbPassword = dbConfig.property("password").getString()
    Database.connect(dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword)

    Flyway.configure()
        .dataSource(dbUrl, dbUser, dbPassword)
        .locations("classpath:db/migration")
        .load()
        .migrate()

    // Configure plugins and features
    configureFrameworks()
    configureLogging()
    configureDocumentation()
    configureSerialization()
    configureAdministration()
    configureHTTP()

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowCredentials = true
    }

    install(ContentNegotiation) {
        json()
    }
    // Configure routing (should be last)
    configureRouting()
}
