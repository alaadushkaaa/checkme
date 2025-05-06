package checkme.web.solution.forms

import checkme.domain.models.User
import org.http4k.lens.MultipartForm

data class CheckSolutionRequest (
    val authUser: User,
    val taskId: String,
    val form: MultipartForm,
)
