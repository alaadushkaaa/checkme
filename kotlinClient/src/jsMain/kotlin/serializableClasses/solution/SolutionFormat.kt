package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable
import ru.yarsu.serializableClasses.task.TaskIdName

@Serializable
data class SolutionFormat(
    val status: String,
    val result: Map<String, ResultScoreMessage>?,
    val task: TaskIdName,
    val totalScore: Int?
)