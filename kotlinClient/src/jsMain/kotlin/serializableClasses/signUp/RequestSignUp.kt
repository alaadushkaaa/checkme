package ru.yarsu.serializableClasses.signUp

import kotlinx.serialization.Serializable

@Serializable
data class RequestSignUp(
    val username: String,
    val name: String,
    val surname: String,
    val password: String
)