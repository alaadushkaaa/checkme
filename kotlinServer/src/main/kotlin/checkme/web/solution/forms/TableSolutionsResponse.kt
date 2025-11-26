package checkme.web.solution.forms

import checkme.domain.models.Check

data class TableSolutionsResponse (
    val tasks: List<TaskIdAndName>,
    val users: List<UserDataForUsersList>,
    val solutions: Map<Int, List<Check>>,
)
