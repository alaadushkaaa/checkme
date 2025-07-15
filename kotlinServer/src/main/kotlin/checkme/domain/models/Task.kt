package checkme.domain.models

import checkme.domain.checks.Criterion

data class Task(
    val id: Int,
    val name: String,
    val criterions: Map<String, Criterion>,
    val answerFormat: List<FormatOfAnswer>,
    val description: String,
)
