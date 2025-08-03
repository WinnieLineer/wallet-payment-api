package com.example.wallet

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.flywaydb.core.Flyway
import java.sql.DriverManager

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val dbConfig = environment.config.config("db")
    val dbUrl = dbConfig.property("url").getString()
    val dbUser = dbConfig.property("user").getString()
    val dbPassword = dbConfig.property("password").getString()

    println("DB URL: $dbUrl")
    println("Loaded drivers: " + DriverManager.getDrivers().toList())

    Database.connect(dbUrl, driver = "org.postgresql.Driver", user = dbUser, password = dbPassword)

    Flyway.configure()
        .dataSource(dbUrl,dbUser,dbPassword)
        .locations("classpath:db/migration")
        .load()
        .migrate()

    configureRouting()


    configureFrameworks()
    configureSerialization()
    configureMonitoring()
    configureSockets()
    configureAdministration()
    configureHTTP()
}
