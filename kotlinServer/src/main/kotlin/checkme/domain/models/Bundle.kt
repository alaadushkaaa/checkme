package checkme.domain.models

data class Bundle(
    val id: Int,
    val name: String,
    val tasks: Map<Int, Int>?,
    val isActual: Boolean,
)
