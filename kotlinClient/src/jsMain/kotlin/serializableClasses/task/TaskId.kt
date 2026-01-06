package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class TaskId(
    val taskId: Uuid
)