package checkme.domain.forms

import checkme.domain.models.User

data class CheckSolutionRequest (
    val authUser: User,
    val taskId: String,
)
