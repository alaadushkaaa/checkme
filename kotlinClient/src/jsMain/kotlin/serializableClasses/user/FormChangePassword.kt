package ru.yarsu.serializableClasses.user

import kotlinx.serialization.Serializable

@Serializable
data class FormChangePassword (
    val oldPassword: String,
    val newPassword: String
)