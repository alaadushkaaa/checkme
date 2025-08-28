package ru.yarsu.serializableClasses.signIn

import kotlinx.serialization.Serializable

@Serializable
data class RequestSignIn(
    val username: String,
    val password: String
)