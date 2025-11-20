package checkme.web.bundles.forms

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder

data class BundleTasksResponse(
    val bundle: Bundle,
    val tasks: List<TaskAndOrder>,
)
