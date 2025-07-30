package checkme.web.solution.forms

import checkme.domain.forms.CheckResult

data class ResultResponse(
    val status: String,
    val result: Map<String, CheckResult>?,
    val task: TaskResultResponse
)

data class TaskResultResponse(
    val _id: String,
    val name: String
)