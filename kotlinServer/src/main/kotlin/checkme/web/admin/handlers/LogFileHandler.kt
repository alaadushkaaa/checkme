package checkme.web.admin.handlers

import checkme.domain.models.User
import checkme.logging.LogObject
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.fileNameOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

class LogFileHandler(
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val fileName = request.fileNameOrNull()
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)

        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(ViewJournalError.USER_CANT_VIEW_JOURNAL.errorText)

            fileName == null -> objectMapper.sendBadRequestError(ViewJournalError.NO_FILE_NAME_TO_VIEW_LOGS.errorText)

            else -> return sendLogFileContentResponse(
                objectMapper = objectMapper,
                fileName = fileName,
                user = user
            )
        }
    }
}

@Suppress("TooGenericExceptionCaught")
private fun sendLogFileContentResponse(
    objectMapper: ObjectMapper,
    fileName: String,
    user: User,
): Response {
    return try {
        val logFile = File("logs", fileName)
        when {
            !logFile.exists() -> objectMapper.sendBadRequestError(ViewJournalError.LOG_FILE_NOT_EXISTS.errorText)
            !logFile.name.matches(Regex("server(\\.\\d{4}-\\d{2}-\\d{2}\\.\\d+)?\\.log(\\.gz)?")) ->
                objectMapper.sendBadRequestError(ViewJournalError.INCORRECT_LOG_FILE_NAME.errorText)

            else -> {
                val logs = if (fileName.endsWith(".gz")) {
                    readGzipFile(
                        file = logFile,
                        objectMapper = objectMapper
                    )
                } else {
                    readFile(
                        file = logFile,
                        objectMapper = objectMapper
                    )
                }
                objectMapper.sendOKResponse(logs.sortedByDescending { it.date })
            }
        }
    } catch (e: Exception) {
        ServerLogger.log(
            user = user,
            action = "Read log file",
            message = "Error reading log file $fileName: ${e.message}",
            type = LoggerType.WARN
        )
        objectMapper.sendBadRequestError(e.message)
    }
}

private fun readFile(
    file: File,
    objectMapper: ObjectMapper,
): List<LogObject> {
    return file.bufferedReader().useLines { lines ->
        lines.toList().map { line ->
            objectMapper.readValue<LogObject>(line)
        }
    }
}

private fun readGzipFile(
    file: File,
    objectMapper: ObjectMapper,
): List<LogObject> {
    return GZIPInputStream(FileInputStream(file)).bufferedReader().use { reader ->
        reader.readLines().map { line ->
            objectMapper.readValue<LogObject>(line)
        }
    }
}
