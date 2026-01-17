package checkme.web.bundles

import checkme.domain.operations.OperationHolder
import checkme.web.bundles.handlers.AddBundleHandler
import checkme.web.bundles.handlers.BundleHandler
import checkme.web.bundles.handlers.BundleHiddenListHandler
import checkme.web.bundles.handlers.BundleListHandler
import checkme.web.bundles.handlers.BundleTasksHandler
import checkme.web.bundles.handlers.ChangeBundleActualityHandler
import checkme.web.bundles.handlers.ChangeBundleNameHandler
import checkme.web.bundles.handlers.ChangeBundleTasksOrderHandler
import checkme.web.bundles.handlers.DeleteBundleHandler
import checkme.web.bundles.handlers.SelectBundleTasks
import checkme.web.context.ContextTools
import org.http4k.core.*
import org.http4k.routing.*

fun bundleRouter(
    operations: OperationHolder,
    contextTools: ContextTools,
): RoutingHttpHandler =
    routes(
        NEW_BUNDLE bind Method.POST to AddBundleHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens,
        ),
        "$CHANGE_ACTUALITY/{id}" bind Method.POST to ChangeBundleActualityHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens,
        ),
        "$CHANGE_NAME/{id}" bind Method.POST to ChangeBundleNameHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens
        ),
        "$DELETE_BUNDLE/{id}" bind Method.DELETE to DeleteBundleHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens
        ),
        "/all" bind Method.GET to BundleListHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens,
        ),
        "/hidden" bind Method.GET to BundleHiddenListHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens,
        ),
        "/select-tasks/{id}" bind Method.POST to SelectBundleTasks(
            userLens = contextTools.userLens,
            taskOperations = operations.taskOperations,
            bundleOperations = operations.bundleOperations
        ),
        "/select-order/{id}" bind Method.POST to ChangeBundleTasksOrderHandler(
            userLens = contextTools.userLens,
            bundleOperations = operations.bundleOperations
        ),
        "/tasks/{id}" bind Method.GET to BundleTasksHandler(
            userLens = contextTools.userLens,
            bundleOperations = operations.bundleOperations
        ),
        "/{id}" bind Method.GET to BundleHandler(
            userLens = contextTools.userLens,
            bundleOperations = operations.bundleOperations,
        )
    )

const val BUNDLE_SEGMENT = "/bundle"
const val NEW_BUNDLE = "/new"
const val CHANGE_ACTUALITY = "/change-actuality"
const val CHANGE_NAME = "/change-name"
const val DELETE_BUNDLE = "/delete"
