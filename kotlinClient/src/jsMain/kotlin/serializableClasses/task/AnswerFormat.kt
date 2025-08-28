package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable

@Serializable
data class AnswerFormat(
    val name: String,
    val type: String
)