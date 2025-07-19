package ru.yarsu.serializableClasses

import kotlinx.serialization.Serializable

@Serializable
data class ResponseUnauthorized(
    val error: String
)