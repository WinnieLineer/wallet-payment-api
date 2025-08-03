package com.example.wallet.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.java.time.timestamp
import java.time.Instant
import java.util.*

object Users : Table() {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 50)
    val createdAt = timestamp("created_at")
    
    override val primaryKey = PrimaryKey(id)
}

data class User(
    val id: UUID,
    val name: String,
    val createdAt: Instant
)

data class CreateUserRequest(
    val name: String
)