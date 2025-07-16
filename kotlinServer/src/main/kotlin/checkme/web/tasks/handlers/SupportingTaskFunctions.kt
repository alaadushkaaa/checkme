package checkme.web.tasks.handlers

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.FormatOfAnswer
import checkme.domain.models.Task
import checkme.domain.operations.tasks.CreateTaskError
import checkme.domain.operations.tasks.TaskFetchingError
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

internal fun fetchTask(
    taskId: Int,
    taskOperations: TaskOperationsHolder,
): Result<Task, FetchingTaskError> {
    return when (
        val fetchedTask = taskOperations.fetchTaskById(taskId)
    ) {
        is Success -> Success(fetchedTask.value)
        is Failure -> when (fetchedTask.reason) {
            TaskFetchingError.NO_SUCH_TASK -> Failure(FetchingTaskError.NO_SUCH_TASK)
            TaskFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingTaskError.UNKNOWN_DATABASE_ERROR)
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
    val answerFormatFromForm: List<FormatOfAnswer> =
        jacksonMapper.readValue<List<FormatOfAnswer>>(TaskLenses.answerFormatField(this).value)
    val answerFormatBd = answerFormatFromForm.associate { it.name to AnswerType.valueOf(it.type) }
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
            answerFormat = answerFormatBd,
            description = description
        )
    )
}

fun Task.tryAddTaskToDirectory(files: Map<String, List<MultipartFormFile>>) {
    // todo проверить сохранение файлов со специальными проверками
    val tasksDir = File(
        "..$TASKS_DIR" +
            "/${this.name.trim()}" +
            "-${this.id}"
    )
    if (!tasksDir.exists()) {
        tasksDir.mkdirs()
    }
    for (file in files.values.flatten()) {
        val filePath = File(tasksDir, file.filename)
        val fileBytes = file.content.use { it.readAllBytes() }
        filePath.writeBytes(fileBytes)
    }
}

enum class CreationTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
}

enum class ValidateTaskError(val errorText: String) {
    NO_SUCH_FILE_FOR_CRITERION("All specified files must be added"),
}

enum class FetchingTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_TASK("The task does not exist"),
}
