package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable

@Serializable
data class TaskId(
    val taskId: Int
)