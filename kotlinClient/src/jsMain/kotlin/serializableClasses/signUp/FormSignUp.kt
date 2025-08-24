package ru.yarsu.serializableClasses.signUp

import kotlinx.serialization.Serializable

@Serializable
data class FormSignUp (
    val login: String,
    val name: String,
    val surname: String,
    val password: String,
    val passwordRepeat: String
)