package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable

@Serializable
data class TaskName(
    val name: String
)