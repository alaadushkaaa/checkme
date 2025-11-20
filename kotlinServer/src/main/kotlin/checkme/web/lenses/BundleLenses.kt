package checkme.web.lenses

import org.http4k.core.*
import org.http4k.lens.MultipartFormField
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm

object BundleLenses {
    val nameField = MultipartFormField.required("name")

    val multipartFormFieldsAll = Body.Companion
        .multipartForm(
            Validator.Feedback,
            nameField
        ).toLens()
}
