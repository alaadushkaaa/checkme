package ru.yarsu.serializableClasses.solution

import kotlinx.serialization.Serializable

@Serializable
data class ResultScoreMessage(
    val score: Int,
    val message: String
)