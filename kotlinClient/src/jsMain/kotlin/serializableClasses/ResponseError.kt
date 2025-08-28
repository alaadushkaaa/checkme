package ru.yarsu.serializableClasses

import kotlinx.serialization.Serializable

@Serializable
data class ResponseError(
    val error: String
)