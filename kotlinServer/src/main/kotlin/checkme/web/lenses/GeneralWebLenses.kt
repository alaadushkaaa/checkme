package checkme.web.lenses

import checkme.domain.models.User
import org.http4k.core.*
import org.http4k.lens.*

object GeneralWebLenses {

    fun userLens(contexts: RequestContexts): RequestContextLens<User?> =
        RequestContextKey
            .optional(contexts, "user")

    /**
     * Lens for getting {id} from request path
     */
    val idFromPathField = Path.int().of("id")
    val checkIdFromPathField = Path.int().of("checkId")
    val userIdFromPathField = Path.int().of("user")
    val taskIdFromPathField = Path.int().of("task")
    val pageCountFromPathField = Path.int().of("page")
// todo когда появится интерфейс для прсомотра результатов по конкретному заданию, конкретного пользователя и т.д.

//    val userIdQuery = Query.int().optional("user")
//    val taskIdkQuery = Query.int().optional("task")
//    val pageCountQuery = Query.int().defaulted("page", 1)

    /**
     * Invoke lens to value
     * @return invoked value or null, when was thrown LensFailure
     */
    fun <IN : Any, OUT> lensOrNull(
        lens: Lens<IN, OUT?>,
        value: IN,
    ): OUT? =
        try {
            lens.invoke(value)
        } catch (_: LensFailure) {
            null
        }

    /**
     * Invoke lens to value
     * @return invoked value or default, when was thrown LensFailure
     */
    fun <IN : Any, OUT> lensOrDefault(
        lens: Lens<IN, OUT>,
        value: IN,
        default: OUT,
    ): OUT =
        try {
            lens.invoke(value)
        } catch (_: LensFailure) {
            default
        }

    /**
     * Create BiDiLens for WebForm by fields lenses and validator
     */
    fun makeBodyLensForFields(
        vararg formFields: BiDiLens<WebForm, *>,
        validator: Validator? = null,
    ): BiDiBodyLens<WebForm> =
        Body.webForm(
            validator ?: Validator.Feedback,
            *formFields,
        ).toLens()

    /**
     * Syntax sugar for invocation lenses methods
     */
    infix fun BiDiBodyLens<WebForm>.from(request: Request) = this(request)

    infix fun BiDiBodyLens<MultipartForm>.from(request: Request) = this(request)

    infix fun <T> BiDiLens<WebForm, T>.from(form: WebForm) = this(form)

    infix fun <T> BiDiLens<MultipartForm, T>.from(form: MultipartForm) = this(form)

    fun Request.idOrNull() = lensOrNull(idFromPathField, this)

    fun Request.checkIdOrNull() = lensOrNull(checkIdFromPathField, this)

    fun Request.userIdOrNull() = lensOrNull(userIdFromPathField, this)

    fun Request.taskIdOrNull() = lensOrNull(taskIdFromPathField, this)

    fun Request.pageCountOrNull() = lensOrNull(pageCountFromPathField, this)
}
