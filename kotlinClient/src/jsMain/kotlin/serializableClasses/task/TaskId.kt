package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class TaskId(
    @OptIn(ExperimentalUuidApi::class)
    val taskId: Uuid
)