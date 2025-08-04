package checkme.web.solution.forms

import checkme.domain.forms.CheckResult

data class CheckWithAllData(
    val id: String,
    val date: String,
    val status: String,
    val result: Map<String, CheckResult>?,
    val user: UserDataForAllResults,
    val task: TaskDataForAllResults,
)

data class CheckWithTaskData(
    val id: String,
    val date: String,
    val status: String,
    val task: TaskDataForAllResults,
)

data class TaskDataForAllResults(
    val name: String,
)

data class UserDataForAllResults(
    val name: String,
    val surname: String,
)
