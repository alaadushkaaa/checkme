package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.Check
import checkme.domain.operations.checks.CreateCheckError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

class CreateBundle(
    private val insertBundle: (
            name: String,
            tasks: Map<Int, Int>,
            isActual: Boolean,
    ) -> Bundle?,
) : (
        String,
        Map<Int, Int>,
        Boolean,
    ) -> Result4k<Bundle, CreateBundleError> {
    override fun invoke(
        name: String,
        tasks: Map<Int, Int>,
        isActual: Boolean
    ): Result4k<Bundle, CreateBundleError> =
        when (
            val newBundle = insertBundle(
                name,
                tasks,
                isActual
            )
        ) {
            is Bundle -> Success(newBundle)
            else -> {
                Failure(CreateBundleError.UNKNOWN_DATABASE_ERROR)
            }
        }
}

enum class CreateBundleError {
    UNKNOWN_DATABASE_ERROR,
}