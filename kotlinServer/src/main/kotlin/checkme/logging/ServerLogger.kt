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
        user: User? = null,
        action: String,
        message: String,
        type: LoggerType,
    ) {
        val logObject = LogObject(
            level = type.code,
            date = LocalDateTime.now().format(dateFormatter),
            userId = (user?.id ?: -404).toString(),
            userName = user?.name ?: "unknown",
            userSurname = user?.surname ?: "unknown",
            action = action,
            message = message
        )

        when (type.code) {
            LoggerType.INFO.code -> logger.info(objectMapper.writeValueAsString(logObject))
            LoggerType.WARN.code -> logger.warn(objectMapper.writeValueAsString(logObject))
        }
    }
}
