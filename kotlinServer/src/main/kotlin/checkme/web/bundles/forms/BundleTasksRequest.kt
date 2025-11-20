package checkme.web.bundles.forms

import checkme.domain.models.Task

data class BundleTasksRequest(
    val task: Task,
    val order: Int,
)
