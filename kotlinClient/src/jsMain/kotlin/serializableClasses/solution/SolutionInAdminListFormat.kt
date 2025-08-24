package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable
import ru.yarsu.serializableClasses.task.TaskName

@Serializable
data class UserNameSurname(
    val name: String,
    val surname: String
)

@Serializable
data class SolutionFormatForAdminAllSolutionList(
    val id: Int,
    val date: String,
    val status: String,
    val result: Map<String, ResultScoreMessage>?,
    val user: UserNameSurname,
    val task: TaskName
)
