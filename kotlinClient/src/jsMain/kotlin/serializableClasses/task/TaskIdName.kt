package ru.yarsu.serializableClasses.task

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class TaskIdName(
    val id: Uuid,
    val name: String
)