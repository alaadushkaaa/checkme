package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable
import io.kvision.types.KFile

@Serializable
data class FormAddTask(
    val name: String,
    val description: String,
    val criterion: String,
    val answer: String,
    val format: String,
    val files: List<KFile>? = null
)

@Serializable
data class FormAddTaskFileSelection(
    val beforeEach: String? = null,
    val afterEach: String? = null,
    val beforeAll: String? = null,
    val afterAll: String? = null,
    val fileSelect: String
)

