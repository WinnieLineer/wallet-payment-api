package com.example.wallet.models

import com.example.wallet.common.InstantSerializer
import com.example.wallet.common.UUIDSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

object Users : UUIDTable() {
    val name = varchar("name", 50)
    val createdAt = timestamp("created_at")
}

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
)

@Serializable
data class CreateUserRequest(
    val name: String,
)
