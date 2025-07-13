package checkme.web.tasks

import checkme.domain.operations.OperationHolder
import checkme.web.tasks.handlers.AddTaskHandler
import org.http4k.core.*
import org.http4k.routing.*

fun taskRouter(operations: OperationHolder): RoutingHttpHandler =
    routes(
        NEW_TASK bind Method.POST to AddTaskHandler(operations.taskOperations)
    )

const val TASK_SEGMENT = "/task"
const val NEW_TASK = "/new"
