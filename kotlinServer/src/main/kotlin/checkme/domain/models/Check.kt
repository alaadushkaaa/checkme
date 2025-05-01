package checkme.domain.models

import java.util.Date

data class Check (
    val taskId: Int,
    val userId: Int,
    val date: Date,
    val status: String,
)

enum class CheckType(val code: String) {
    CONSOLE_CHECK("console-check"),
}
