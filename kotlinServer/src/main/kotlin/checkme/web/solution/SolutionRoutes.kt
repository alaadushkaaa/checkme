package checkme.web.solution

import checkme.domain.operations.OperationHolder
import checkme.web.context.ContextTools
import checkme.web.solution.handlers.CheckSolutionHandler
import org.http4k.core.*
import org.http4k.routing.*

fun solutionRouter(
    operations: OperationHolder,
    contextTools: ContextTools,
): RoutingHttpHandler =
    routes(
        "$NEW_SOLUTION/{taskId}" bind Method.POST to CheckSolutionHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userLens = contextTools.userLens
        )
    )

const val SOLUTION_SEGMENT = "/solution"
const val NEW_SOLUTION = "/new"
