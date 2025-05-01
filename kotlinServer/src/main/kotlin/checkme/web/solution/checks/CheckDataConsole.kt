package checkme.web.solution.checks

import checkme.domain.models.CheckType

data class CheckDataConsole(
    val type: CheckType,
    val command: String,
    val expected: String,
)
