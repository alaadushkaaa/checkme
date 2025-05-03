package checkme.domain.models

import checkme.web.solution.forms.CheckResult
import java.time.LocalDateTime

data class Check (
    val id: Int,
    val taskId: Int,
    val userId: Int,
    val date: LocalDateTime,
    val result: Map<String, CheckResult>?,
    val status: String,
)

enum class CheckType(val code: String) {
    CONSOLE_CHECK("console-check"),
}
