package ru.yarsu.serializableClasses.task

import io.kvision.types.KFile
import kotlinx.serialization.Serializable

@Serializable
data class TaskFormat(
    val id: Int,
    val name: String,
    val criterions: Map<String, Criterion>,
    val answerFormat: List<AnswerFormat>,
    val description: String
)

@Serializable
data class SolutionFileList(
    val file: List<KFile>
)

@Serializable
data class CheckId(
    val checkId: Int
)