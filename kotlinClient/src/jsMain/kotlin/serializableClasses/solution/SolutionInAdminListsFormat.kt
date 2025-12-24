package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable
import ru.yarsu.serializableClasses.task.TaskFormat
import ru.yarsu.serializableClasses.task.TaskFormatForList
import ru.yarsu.serializableClasses.task.TaskName

@Serializable
data class UserNameSurname(
    val name: String,
    val surname: String
)

@Serializable
data class SolutionInAdminListsFormat(
    val id: Int,
    val date: String,
    val status: String,
    val result: Map<String, ResultScoreMessage>?,
    val user: UserNameSurname? = null,
    val task: TaskName? = null,
    val totalScore: Int?,
)

@Serializable
data class SolutionsGroupByTask(
    val task: TaskFormatForList,
    val solutions: List<SolutionInformationWithUserInformation>
)