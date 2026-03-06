package checkme.web.user.forms

data class ChangePasswordData(
    val oldPassword: String,
    val newPassword: String,
)
