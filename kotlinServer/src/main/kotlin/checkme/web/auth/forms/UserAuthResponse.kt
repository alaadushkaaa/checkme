package checkme.web.auth.forms

data class UserAuthResponse (
    val username: String,
    val name: String,
    val surname: String,
    val token: String
)
