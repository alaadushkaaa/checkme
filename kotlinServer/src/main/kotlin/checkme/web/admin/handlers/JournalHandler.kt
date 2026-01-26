package checkme.web.admin.handlers

import checkme.domain.models.User
import checkme.logging.LogFileInfo
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.pageCountOrNull
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens
import java.io.File
import kotlin.math.min

const val FILE_LIMIT = 15

class JournalHandler(
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val page = request.pageCountOrNull()

        return when {
            user == null -> objectMapper.sendBadRequestError(ViewJournalError.USER_CANT_VIEW_JOURNAL.errorText)
            !user.isAdmin() -> objectMapper.sendBadRequestError(ViewJournalError.USER_CANT_VIEW_JOURNAL.errorText)
            page == null -> objectMapper.sendBadRequestError(ViewJournalError.NO_PAGE_ERROR.errorText)
            else -> {
                val logFiles = getLogFiles(
                    user = user,
                    page = page
                )
                return objectMapper.sendOKResponse(logFiles)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getLogFiles(
        user: User,
        page: Int,
    ): List<LogFileInfo> {
        val logDir = File("logs")
        val logs = mutableListOf<LogFileInfo>()

        try {
            if (!logDir.exists() || !logDir.isDirectory) {
                return logs
            }

            val logFiles = logDir.listFiles { file ->
                file.name.matches(Regex("""server(\.\d{4}-\d{2}-\d{2}\.\d+)?\.log(\.gz)?"""))
            } ?: arrayOf()

            logFiles.sortByDescending { it.lastModified() }

            val startIndex = (page - 1) * FILE_LIMIT
            val endIndex = min(page * FILE_LIMIT, logFiles.size)

            for (file in logFiles.slice(startIndex..endIndex - 1))
                logs.add(
                    LogFileInfo(
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        isCompressed = file.name.endsWith(".gz")
                    )
                )
        } catch (e: Exception) {
            ServerLogger.log(
                user = user,
                action = "Get log files",
                message = "Error when trying to read logs directory: ${e.message}",
                type = LoggerType.WARN
            )
        }

        return logs
    }
}

enum class ViewJournalError(val errorText: String) {
    USER_CANT_VIEW_JOURNAL("User can't view this page"),
    NO_FILE_NAME_TO_VIEW_LOGS("No file name to view logs"),
    LOG_FILE_NOT_EXISTS("File with this name doesn't exists in logs"),
    INCORRECT_LOG_FILE_NAME("Log file name in incorrect"),
    NO_PAGE_ERROR("No page to show journal list"),
}
