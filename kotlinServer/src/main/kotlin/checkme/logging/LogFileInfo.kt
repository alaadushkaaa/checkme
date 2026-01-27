package checkme.logging

data class LogFileInfo(
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isCompressed: Boolean,
)

data class LogObject(
    val level: String,
    val date: String,
    val userId: String,
    val userName: String,
    val userSurname: String,
    val action: String,
    val message: String,
)

enum class LoggerType (val code: String) {
    INFO("INFO"),
    WARN("WARN"),
}
