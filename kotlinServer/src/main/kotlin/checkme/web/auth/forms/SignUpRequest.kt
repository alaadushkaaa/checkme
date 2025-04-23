package checkme.web.auth.forms

data class SignUpRequest(
    val username: String,
    val name: String,
    val surname: String,
    val password: String,
)
