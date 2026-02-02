package checkme.web.solution.forms

import checkme.domain.forms.CheckResult

data class ResultResponse(
    val status: String,
    val result: Map<String, CheckResult>?,
    val task: TaskResultResponse,
    val totalScore: Int?,
)

data class TaskResultResponse(
    val id: String,
    val name: String,
)
