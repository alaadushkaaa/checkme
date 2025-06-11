package checkme.web.solution.handlers

import checkme.domain.models.Check
import checkme.domain.models.User
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import java.io.File

private fun chooseFileExtension(extension: String): String =
    when (extension) {
        "text/x-python" -> ".py"
        "text/x-java" -> ".java"
        "text/x-c++" -> ".cpp"
        "text/x-c" -> ".c"
        "text/javascript" -> ".js"
        "application/json" -> ".json"
        else -> ".txt"
    }

internal fun tryAddFileToUserSolutionDirectory(
    checkId: Int,
    user: User,
    file: MultipartFormFile,
    taskName: String,
): String {
    val solutionDir = File(
        "..$SOLUTIONS_DIR" +
            "/${user.name}-${user.surname}-${user.login}" +
            "/$taskName" +
            "/$checkId"
    )
    if (!solutionDir.exists()) {
        solutionDir.mkdirs()
    }
    val extension = chooseFileExtension(file.contentType.value)
    val filePath = File(solutionDir, "${checkId}$extension")
    val fileBytes = file.content.use { it.readAllBytes() }
    filePath.writeBytes(fileBytes)
    return filePath.absolutePath.toString()
}

internal fun tryGetFieldsAndFilesFromForm(
    filesForm: MultipartForm,
    newCheck: Check,
    user: User,
): List<Pair<String, String>> {
    val fieldAnswers = filesForm.fields
        .map { Pair("field", it.value.toString()) }
    val filesAnswers = filesForm.files
        .map {
            Pair(
                "file",
                tryAddFileToUserSolutionDirectory(
                    checkId = newCheck.id,
                    user = user,
                    file = it.value.first(),
                    taskName = task.name
                )
            )
        }
    return fieldAnswers + filesAnswers
}
