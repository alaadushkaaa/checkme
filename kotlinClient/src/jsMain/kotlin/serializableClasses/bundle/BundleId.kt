package ru.yarsu.serializableClasses.bundle

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class BundleId (
    val bundleId: Uuid
)