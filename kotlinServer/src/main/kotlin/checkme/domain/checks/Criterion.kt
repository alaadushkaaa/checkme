package checkme.domain.checks

data class Criterion(
    val description: String,
    val score: Int,
    val test: String,
    val message: String,
)
