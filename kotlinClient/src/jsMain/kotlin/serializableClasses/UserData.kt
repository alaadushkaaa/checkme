package ru.yarsu.serializableClasses

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val username: String,
    val name: String,
    val surname: String
)