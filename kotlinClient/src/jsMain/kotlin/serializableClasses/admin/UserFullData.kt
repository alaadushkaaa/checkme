package ru.yarsu.serializableClasses.admin

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class UserFullData(
    val id: Uuid,
    val login: String,
    val name: String,
    val surname: String,
    val isSystemPass: Boolean
)