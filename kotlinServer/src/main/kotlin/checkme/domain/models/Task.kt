package checkme.domain.models

import checkme.domain.checks.Criterion
import java.util.UUID

data class Task(
    val id: UUID,
    val name: String,
    val criterions: Map<String, Criterion>,
    val answerFormat: Map<String, AnswerType>,
    val description: String,
    val isActual: Boolean,
    val bestScore: Int? = null,
    val highestScore: Int? = null,
)

enum class AnswerType(val code: String) {
    FILE("file"),
    TEXT("text"),
}

enum class ResultType(val code: String) {
    LIST("list"),
    TASK("task"),
    HIDDEN("hiddenList"),
    ;

    companion object {
        fun resultTypeFromCode(code: String): ResultType? {
            return entries.find { it.code == code }
        }
    }
}
