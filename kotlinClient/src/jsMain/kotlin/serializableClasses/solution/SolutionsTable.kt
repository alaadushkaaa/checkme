package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable
import ru.yarsu.serializableClasses.task.TaskIdName
import ru.yarsu.serializableClasses.user.UserInList
import kotlin.uuid.Uuid

@Serializable
data class SolutionsTable(
    val tasks: List<TaskIdName>,
    val users: List<UserInList>,
    val solutions: Map<Uuid, List<SolutionInformation>>
)

@Serializable
data class SolutionInformation(
    val id: Uuid,
    val taskId: Uuid,
    val userId: Uuid,
    val date: String,
    val result: Map<String, ResultScoreMessage>?,
    val status: String,
    val totalScore: Int?,
)

@Serializable
data class SolutionInformationWithUserInformation(
    val id: Uuid,
    val date: String,
    val status: String,
    val result: Map<String, ResultScoreMessage>?,
    val user: UserNameSurname,
    val totalScore: Int?
)

@Serializable
data class IdScore(
    val id: Uuid,
    val score: Int
)