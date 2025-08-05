package checkme.web.tasks

import checkme.domain.operations.OperationHolder
import checkme.web.context.ContextTools
import checkme.web.tasks.handlers.AddTaskHandler
import checkme.web.tasks.handlers.TaskHandler
import checkme.web.tasks.handlers.TasksListHandler
import org.http4k.core.*
import org.http4k.routing.*

fun taskRouter(
    operations: OperationHolder,
    contextTools: ContextTools
): RoutingHttpHandler =
    routes(
        NEW_TASK bind Method.POST to AddTaskHandler(operations.taskOperations),
        "/all" bind Method.GET to TasksListHandler(
            taskOperations = operations.taskOperations,
            userLens = contextTools.userLens),
        "/{id}" bind Method.GET to TaskHandler(operations.taskOperations)
    )

const val TASK_SEGMENT = "/task"
const val NEW_TASK = "/new"
