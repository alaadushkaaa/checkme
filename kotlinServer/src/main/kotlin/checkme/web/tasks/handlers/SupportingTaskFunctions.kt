package checkme.web.tasks.handlers

import checkme.domain.checks.Criterion
import checkme.domain.models.FormatOfAnswer
import checkme.domain.models.Task
import checkme.domain.operations.tasks.CreateTaskError
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.lenses.TaskLenses
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import java.io.File

internal fun addTask(
    task: Task,
    taskOperations: TaskOperationsHolder,
): Result<Task, CreationTaskError> {
    return when (
        val newTask = taskOperations.createCheck(
            task.name,
            task.criterions,
            task.answerFormat,
            task.description,
        )
    ) {
        is Success -> Success(newTask.value)
        is Failure -> when (newTask.reason) {
            CreateTaskError.UNKNOWN_DATABASE_ERROR -> Failure(CreationTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

// todo add all checks for validate task
fun MultipartForm.validateForm(taskId: Int?): Result<Task, ValidateTaskError> {
    val jacksonMapper = jacksonObjectMapper()
    val taskName = TaskLenses.nameField(this).value
    val description = TaskLenses.descriptionField(this).value
    val criterions: Map<String, Criterion> =
        jacksonMapper.readValue<Map<String, Criterion>>(TaskLenses.criterionsField(this).value)
    val answerFormat = jacksonMapper.readValue<FormatOfAnswer>(TaskLenses.answerFormatField(this).value)
    val files = TaskLenses.filesField(this)
    for (criterion in criterions) {
        if (!files.map { it.filename }.contains(criterion.value.test)) {
            return Failure(ValidateTaskError.NO_SUCH_FILE_FOR_CRITERION)
        }
    }
    return Success(
        Task(
            id = taskId ?: -1,
            name = taskName,
            criterions = criterions,
            answerFormat = answerFormat,
            description = description
        )
    )
}

fun Task.tryAddTaskToDirectory(files: Map<String, List<MultipartFormFile>>) {
    // todo проверить сохранение файлов со специальными проверками
    val tasksDir = File(
        "..$TASKS_DIR" +
            "/${this.name}" +
            "-${this.id}"
    )
    if (!tasksDir.exists()) {
        tasksDir.mkdirs()
    }
    for (file in files) {
        val filePath = File(tasksDir, "${file.value.first().filename}.json")
        val fileBytes = file.value.first().content.use { it.readAllBytes() }
        filePath.writeBytes(fileBytes)
    }
}

enum class CreationTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Что-то произошло. Попробуйте еще раз позднее или обратитесь за помощью"),
}

enum class ValidateTaskError(val errorText: String) {
    NO_SUCH_FILE_FOR_CRITERION("Необходимо добавить все файлы для указанных критериев"),
}
