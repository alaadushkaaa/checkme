package checkme.web.solution.forms

import checkme.domain.forms.CheckResult
import checkme.domain.models.Task

data class ListUserCheck(
    val name: String,
    val surname: String,
    val solutions: List<ChecksForUsersSolutions>,
)

data class ListTaskCheck(
    val name: String,
    val solutions: List<CheckForTasksSolutions>,
)

data class ChecksForUsersSolutions(
    val id: String,
    val date: String,
    val status: String,
    val result: Map<String, CheckResult>?,
    val task: TaskNameForAllResults,
    val totalScore: Int?,
)

data class CheckForTasksSolutions(
    val id: String,
    val date: String,
    val status: String,
    val result: Map<String, CheckResult>?,
    val user: UserNameSurnameForAllResults,
    val totalScore: Int?,
)

data class SolutionsGroupByTask(
    val task: Task,
    val solutions: List<CheckForTasksSolutions>,
)
