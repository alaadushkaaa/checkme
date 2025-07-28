package checkme.web.tasks.forms

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.FormatOfAnswer

data class TaskClientResponse (
    val id: Int,
    val name: String,
    val criterions: Map<String, Criterion>,
    val answerFormat: List<FormatOfAnswer>,
    val description: String,
) {
    companion object {
        fun Map<String, AnswerType>.toClientEntryAnswerFormat(): List<FormatOfAnswer> {
            return this.map { (key, value) ->
                FormatOfAnswer(name = key, type = value.code)
            }
        }
    }
}
