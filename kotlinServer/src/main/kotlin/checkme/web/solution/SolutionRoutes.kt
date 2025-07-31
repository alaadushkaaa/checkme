package checkme.web.solution

import checkme.domain.operations.OperationHolder
import checkme.web.context.ContextTools
import checkme.web.solution.handlers.CheckSolutionHandler
import checkme.web.solution.handlers.ListResultsHandler
import checkme.web.solution.handlers.ResultHandler
import org.http4k.core.*
import org.http4k.routing.*

fun solutionRouter(
    operations: OperationHolder,
    contextTools: ContextTools,
): RoutingHttpHandler =
    routes(
        "$NEW_SOLUTION/{id}" bind Method.POST to CheckSolutionHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userLens = contextTools.userLens
        ),
        "/{checkId}" bind Method.GET to ResultHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userLens = contextTools.userLens
        ),
        "/all/{pageId}" bind Method.GET to ListResultsHandler()
    )

const val SOLUTION_SEGMENT = "/solution"
const val NEW_SOLUTION = "/new"
