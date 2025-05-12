package checkme.web.solution.checks

import checkme.domain.models.CheckType
import checkme.domain.models.Task
import checkme.web.solution.forms.CheckResult

data class CheckDataConsole(
    val type: CheckType,
    val command: String,
    val expected: String,
) {
    companion object {
        fun consoleCheck(
            task: Task,
            answers: MutableList<String>,
            check: CheckDataConsole,
        ) : CheckResult {
            
        }
    }
}
