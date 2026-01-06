package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable
import ru.yarsu.serializableClasses.task.TaskName
import kotlin.uuid.Uuid

@Serializable
data class SolutionFormatForUserSolutionList(
    val id: Uuid,
    val date: String,
    val status: String,
    val result: Map<String, ResultScoreMessage>?,
    val task: TaskName
)
