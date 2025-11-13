package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class FetchBundleById(
    private val fetchBundleById: (Int) -> Bundle?,
) : (Int) -> Result4k<Bundle, BundleFetchingError> {

    override fun invoke(bundleId: Int): Result4k<Bundle, BundleFetchingError> =
        try {
            when (val bundle = fetchBundleById(bundleId)) {
                is Bundle -> Success(bundle)
                else -> Failure(BundleFetchingError.NO_SUCH_BUNDLE)
            }
        } catch (_: DataAccessException) {
            Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllBundles(
    private val fetchAllBundles: () -> List<Bundle>?,
) : () -> Result4k<List<Bundle>, BundleFetchingError> {

    override fun invoke(): Result4k<List<Bundle>, BundleFetchingError> =
        try {
            when (val bundles = fetchAllBundles()) {
                is List<Bundle> -> Success(bundles)
                else -> Failure(BundleFetchingError.NO_SUCH_BUNDLE)
            }
        } catch (_: DataAccessException) {
            Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchHiddenBundles(
    private val fetchHiddenBundles: () -> List<Bundle>?,
) : () -> Result4k<List<Bundle>, BundleFetchingError> {

    override fun invoke(): Result4k<List<Bundle>, BundleFetchingError> =
        try {
            when (val bundles = fetchHiddenBundles()) {
                is List<Bundle> -> Success(bundles)
                else -> Failure(BundleFetchingError.NO_SUCH_BUNDLE)
            }
        } catch (_: DataAccessException) {
            Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchBundleTasks(
    private val selectBundleTasks: (Int) -> List<TaskAndOrder>?,
) : (Int) -> Result4k<List<TaskAndOrder>, BundleFetchingError> {
    override fun invoke(bundleId: Int): Result4k<List<TaskAndOrder>, BundleFetchingError> {
        return when (val tasks = selectBundleTasks(bundleId)) {
            is List<TaskAndOrder> -> Success(tasks)
            else -> Failure(BundleFetchingError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

enum class BundleFetchingError {
    UNKNOWN_DATABASE_ERROR,
    NO_SUCH_BUNDLE,
}
