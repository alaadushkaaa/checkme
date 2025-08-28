package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable

@Serializable
data class Criterion(
    val description: String,
    val score: Int,
    val test: String,
    val message: String,
)