package ru.yarsu.serializableClasses.bundle

import kotlinx.serialization.Serializable
import ru.yarsu.serializableClasses.task.TaskFormatForList
import kotlin.uuid.Uuid

@Serializable
data class BundleFormat(
    val id: Uuid,
    val name: String,
    val isActual: Boolean,
)

@Serializable
data class TaskFormatWithOrder(
    val task: TaskFormatForList,
    val order: Int,
)

@Serializable
data class BundleFormatWithTasks(
    val bundle: BundleFormat,
    val tasks: List<TaskFormatWithOrder>
)
