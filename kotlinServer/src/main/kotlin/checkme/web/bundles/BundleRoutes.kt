package checkme.web.bundles

import checkme.domain.operations.OperationHolder
import checkme.web.bundles.handlers.AddBundleHandler
import checkme.web.bundles.handlers.AddBundleTasksHandler
import checkme.web.bundles.handlers.BundleHandler
import checkme.web.bundles.handlers.BundleHiddenListHandler
import checkme.web.bundles.handlers.BundleListHandler
import checkme.web.bundles.handlers.SelectTasksOrderHandler
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
        "/all" bind Method.GET to BundleListHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens,
        ),
        "/hidden" bind Method.GET to BundleHiddenListHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens,
        ),
        "/add-tasks/{id}" bind Method.POST to AddBundleTasksHandler(
            bundleOperations = operations.bundleOperations,
            userLens = contextTools.userLens,
        ),
        "/select-order/{id}" bind Method.GET to SelectTasksOrderHandler(
            userLens = contextTools.userLens,
            taskOperations = operations.taskOperations
        ),
        "/{id}" bind Method.GET to BundleHandler(
            userLens = contextTools.userLens,
            bundleOperations = operations.bundleOperations,
        )
    )

const val BUNDLE_SEGMENT = "/bundle"
const val NEW_BUNDLE = "/new"
