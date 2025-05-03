package checkme.web.solution.forms

import checkme.domain.models.User
import org.http4k.lens.MultipartForm

data class CheckSolutionRequest (
    val auth_user: User,
    val task_id: String,
    val form: MultipartForm,
)
