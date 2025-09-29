package checkme.logging

import checkme.domain.models.User
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ServerLogger {
    private val logger = LoggerFactory.getLogger("SERVER_LOGGER")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")
    private val objectMapper = jacksonObjectMapper()

    fun log(
        user: User,
        action: String,
        message: String,
        type: LoggerType,
    ) {
        val logObject = LogObject(
            date = LocalDateTime.now().format(dateFormatter),
            userId = user.id,
            userName = user.name,
            userSurname = user.surname,
            action = action,
            message = message
        )

        when (type.code) {
            LoggerType.INFO.code -> logger.info(objectMapper.writeValueAsString(logObject))
            LoggerType.WARNING.code -> logger.warn(objectMapper.writeValueAsString(logObject))
        }
    }
}

data class LogObject(
    val date: String,
    val userId: Int,
    val userName: String,
    val userSurname: String,
    val action: String,
    val message: String,
)

enum class LoggerType (val code: String) {
    INFO("file"),
    WARNING("text"),
}
