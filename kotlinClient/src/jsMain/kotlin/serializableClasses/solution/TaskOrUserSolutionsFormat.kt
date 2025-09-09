package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable

@Serializable
data class TaskOrUserSolutionsFormat(
    val name: String,
    val surname: String? = null,
    val solutions: List<SolutionInAdminListsFormat>
)
