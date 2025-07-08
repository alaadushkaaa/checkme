package checkme.web.lenses

import org.http4k.core.*
import org.http4k.lens.*

object TaskLenses {
    val taskIdPathField = Path.int().of("taskId")

    val nameField = MultipartFormField.required("name")
    val descriptionField = MultipartFormField.required("description")
    val criterionsField = MultipartFormField.required("criterions")
    val answerFormatField = MultipartFormField.required("answerFormat")
    val filesField = MultipartFormFile.multi.required("file")
    val multipartFormFieldsAll = Body.Companion
        .multipartForm(
            Validator.Feedback,
            nameField,
            descriptionField,
            criterionsField,
            answerFormatField,
            filesField
        ).toLens()
}
