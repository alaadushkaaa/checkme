package checkme.domain.models

import checkme.domain.checks.Criterion

data class Task(
    val id: Int,
    val name: String,
    val criterions: Map<String, Criterion>,
    val answerFormat: FormatOfAnswer,
    val description: String,
)

enum class FormatOfAnswer(val code: String) {
    FILE("file"),
    TEXT("text"),
}
