package checkme.web.solution.forms

import checkme.domain.models.Check

data class TableSolutionsResponse (
    val tasks: List<TaskIdAndName>,
    val solutions: Map<UserDataForUsersList, List<Check>>,
)
