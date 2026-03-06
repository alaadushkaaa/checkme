package checkme.web.lenses

import org.http4k.core.Body
import org.http4k.lens.MultipartFormField
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm

object ChangePasswordLenses {
    val oldPasswordField = MultipartFormField.required("old-password")

    val newPasswordField = MultipartFormField.required("new-password")

    val multiPartFormFieldsAll = Body.Companion
        .multipartForm(
            Validator.Feedback,
            oldPasswordField,
            newPasswordField
        ).toLens()
}
