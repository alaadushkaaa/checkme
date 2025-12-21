package checkme.domain.models

data class Bundle(
    val id: Int,
    val name: String,
    val isActual: Boolean,
)

data class TaskAndOrder(
    val task: Task,
    val order: Int,
)

data class BundleAndTasks(
    val bundle: Bundle,
    val tasks: List<TaskAndOrder>
)
