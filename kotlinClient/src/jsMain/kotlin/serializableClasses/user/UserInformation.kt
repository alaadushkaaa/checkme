package ru.yarsu.serializableClasses.user

import kotlinx.serialization.Serializable

@Serializable
data class UserInformation(
    val username: String,
    val name: String,
    val surname: String,
    val token: String
)