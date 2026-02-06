package checkme.web.lenses

import org.http4k.core.Body
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm

object RegistrationStudentsLenses {
    val fileField = MultipartFormFile.required("file")

    val formField = Body.Companion
        .multipartForm(
            Validator.Feedback,
            fileField
        ).toLens()
}
