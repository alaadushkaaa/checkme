package checkme.domain.models

data class Bundle(
    val id: Int,
    val name: String,
    val isActual: Boolean,
)

data class TaskAndPriority(
    val task: Task,
    val priority: Int,
)
