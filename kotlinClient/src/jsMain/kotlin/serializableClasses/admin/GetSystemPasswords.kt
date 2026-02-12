package ru.yarsu.serializableClasses.admin

import io.kvision.types.KFile
import kotlinx.serialization.Serializable

@Serializable
class GetSystemPasswords(
    val studentsData: List<KFile>? = null,
)