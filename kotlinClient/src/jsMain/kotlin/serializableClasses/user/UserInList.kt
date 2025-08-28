package ru.yarsu.serializableClasses.user

import kotlinx.serialization.Serializable

@Serializable
data class UserInList(
    val id: Int,
    val login: String,
    val name: String,
    val surname: String
)