package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable

@Serializable
data class TaskIdName(
    val id: Int,
    val name: String
)