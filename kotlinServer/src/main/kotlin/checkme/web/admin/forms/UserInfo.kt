package checkme.web.admin.forms

import java.util.UUID

data class UserInfo(
    val id: UUID,
    val login: String,
    val name: String,
    val surname: String,
    val isSystemPass: Boolean,
)
