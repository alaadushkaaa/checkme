package checkme.web.solution.checks

import org.http4k.filter.GzipCompressionMode

data class Criterion(
    val description: String,
    val score: Int,
    val test: String,
    val message: String,
)
