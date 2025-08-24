package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable
import ru.yarsu.serializableClasses.task.TaskName

@Serializable
data class SolutionInListFormat(
    val id: Int,
    val date: String,
    val status: String,
    val task: TaskName
)