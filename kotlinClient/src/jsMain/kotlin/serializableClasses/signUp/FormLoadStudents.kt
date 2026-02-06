package ru.yarsu.serializableClasses.signUp

import io.kvision.types.KFile
import kotlinx.serialization.Serializable

@Serializable
class FormLoadStudents(
    val studentsData: List<KFile>? = null,
)