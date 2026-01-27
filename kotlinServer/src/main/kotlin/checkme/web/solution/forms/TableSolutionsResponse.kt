package checkme.web.solution.forms

import checkme.domain.models.Check
import java.util.UUID

data class TableSolutionsResponse (
    val tasks: List<TaskIdAndName>,
    val users: List<UserDataForUsersList>,
    val solutions: Map<UUID, List<Check>>,
)
