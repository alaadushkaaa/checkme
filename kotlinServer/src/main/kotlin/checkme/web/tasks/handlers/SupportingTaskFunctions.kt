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
import org.http4k.lens.MultipartFormField
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

internal fun taskExists(
    taskId: Int,
    taskOperations: TaskOperationsHolder,
): Boolean {
    return when (fetchTask(taskId = taskId, taskOperations = taskOperations)) {
        is Success -> true
        is Failure -> false
    }
}

@Suppress("ReturnCount")
fun MultipartForm.validateForm(taskId: Int?): Result<Task, ValidateTaskError> {
    val jacksonMapper = jacksonObjectMapper()
    val taskName = TaskLenses.nameField(this).value
    val description = TaskLenses.descriptionField(this).value
    val criterions: Map<String, Criterion> =
        jacksonMapper.readValue<Map<String, Criterion>>(TaskLenses.criterionsField(this).value)
    val answerFormatFromForm: List<FormatOfAnswer> =
        jacksonMapper.readValue<List<FormatOfAnswer>>(TaskLenses.answerFormatField(this).value)

    for (answerFormat in answerFormatFromForm) {
        try {
            AnswerType.valueOf(answerFormat.type.uppercase())
        } catch (_: IllegalArgumentException) {
            return Failure(ValidateTaskError.ANSWER_TYPE_ERROR)
        }
    }
    val answerFormatBd = answerFormatFromForm.associate { it.name to AnswerType.valueOf(it.type.uppercase()) }
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

fun Task.addTaskToDirectory(
    files: Map<String, List<MultipartFormFile>>,
    fields: Map<String, List<MultipartFormField>>,
    criterions: Map<String, Criterion>,
): Map<String, Criterion> {
    val tasksDir = File(
        "..$TASKS_DIR" +
            "/${this.id}" +
            "-${this.name.trim()}"
    )
    if (!tasksDir.exists()) {
        tasksDir.mkdirs()
    }
    for (file in files.values.flatten()) {
        val filePath = File(tasksDir, file.filename)
        val fileBytes = file.content.use { it.readAllBytes() }
        filePath.writeBytes(fileBytes)
    }
    return tryRenameFileAndUpdateCriterions(
        criterions = criterions,
        fields = fields,
        tasksDir = tasksDir
    )
}

fun tryRenameFileAndUpdateCriterions(
    criterions: Map<String, Criterion>,
    fields: Map<String, List<MultipartFormField>>,
    tasksDir: File,
): Map<String, Criterion> {
    val updatedCriterions = criterions.toMutableMap()
    val specialCriteria = listOf("beforeAll", "beforeEach", "afterEach", "afterAll")
    for (criteria in specialCriteria) {
        fields[criteria]?.firstOrNull()?.value?.takeIf { it.isNotBlank() }?.let { originalFileName ->
            val originalFile = File(tasksDir, originalFileName)

            if (originalFile.exists()) {
                val newFile = File(tasksDir, "$criteria.json")
                originalFile.renameTo(newFile)
                println("Renamed $originalFileName to ${newFile.name} for criteria $criteria")

                updatedCriterions.forEach { (key, value) ->
                    if (value.test == originalFileName) {
                        updatedCriterions[key] = value.copy(test = criteria)
                    }
                }
            } else {
                println("Warning: file $originalFileName not found for criteria $criteria")
            }
        }
    }
    return updatedCriterions
}

enum class CreationTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
}

enum class ValidateTaskError(val errorText: String) {
    NO_SUCH_FILE_FOR_CRITERION("All specified files must be added"),
    ANSWER_TYPE_ERROR("This type of task answer does not exist"),
}

enum class FetchingTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_TASK("The task does not exist"),
}
