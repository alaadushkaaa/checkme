package ru.yarsu.serializableClasses.task

import io.kvision.types.KFile
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class TaskFormat(
    val id: Uuid,
    val name: String,
    val criterions: Map<String, Criterion>,
    val answerFormat: List<AnswerFormat>,
    val description: String,
    val isActual: Boolean,
    val bestScore: Int? = null,
    val highestScore: Int? = null
)

@Serializable
data class TaskFormatForList(
    val id: Uuid,
    val name: String,
    val criterions: Map<String, Criterion>,
    val answerFormat: Map<String, String>,
    val description: String,
    val isActual: Boolean,
    val bestScore: Int? = null,
    val highestScore: Int? = null
)

@Serializable
data class SolutionFileList(
    val file: List<KFile>
)

@Serializable
data class CheckId(
    val checkId: Uuid
)