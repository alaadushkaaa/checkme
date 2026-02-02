package ru.yarsu.serializableClasses.logger

import kotlinx.serialization.Serializable

@Serializable
data class LogFormat(
    val level: String,
    val date: String,
    val userId: String,
    val userName: String,
    val userSurname: String,
    val action: String,
    val message: String,
)

@Serializable
data class LogFileInfo(
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isCompressed: Boolean
)