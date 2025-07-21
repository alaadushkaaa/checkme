package ru.yarsu.serializableClasses

import kotlinx.serialization.Serializable

@Serializable
data class FormSignIn (
    val login: String,
    val password: String
)