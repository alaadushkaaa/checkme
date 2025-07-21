package ru.yarsu.serializableClasses

import kotlinx.serialization.Serializable

@Serializable
data class RequestSignIn(
    val username: String,
    val password: String
)
