package ru.yarsu.serializableClasses.user

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserInList(
    val id: Uuid,
    val login: String,
    val name: String,
    val surname: String
)