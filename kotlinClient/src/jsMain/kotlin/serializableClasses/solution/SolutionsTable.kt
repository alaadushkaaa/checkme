package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable
import ru.yarsu.serializableClasses.task.TaskIdName
import ru.yarsu.serializableClasses.user.UserInList

@Serializable
data class SolutionsTable(
    val tasks: List<TaskIdName>,
    val users: List<UserInList>,
    val solutions: Map<Int, List<SolutionInformation>>
)

@Serializable
data class SolutionInformation(
    val id: Int,
    val taskId: Int,
    val userId: Int,
    val date: String,
    val result: Map<String, ResultScoreMessage>?,
    val status: String,
)

@Serializable
data class SolutionInformationWithUserInformation(
    val id: Int,
    val date: String,
    val status: String,
    val result: Map<String, ResultScoreMessage>?,
    val user: UserNameSurname
)

@Serializable
data class IdScore(
    val id: Int,
    val score: Int
)