package checkme.web.tasks.handlers

import checkme.db.tasks.TasksOperations
import checkme.web.lenses.TaskLenses
import org.http4k.core.*
import org.http4k.lens.MultipartForm

class AddTaskHandler(
    private val tasksOperations: TasksOperations,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form: MultipartForm = TaskLenses.multipartFormFieldsAll(request)
        val taskName = TaskLenses.nameField(form).value
        val description = TaskLenses.descriptionField(form).value
        val criterions = TaskLenses.criterionsField(form).value
        val files = TaskLenses.filesField(form)
        val newTask =
            TODO("Not yet implemented")
    }
}
