package ru.yarsu.serializableClasses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseCreated(
    @SerialName("user_data")
    val userData: UserData,
    val token: String
)