package checkme.web.lenses

import checkme.domain.models.User
import org.http4k.core.*
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.lens.Lens
import org.http4k.lens.LensFailure
import org.http4k.lens.MultipartForm
import org.http4k.lens.Path
import org.http4k.lens.RequestContextKey
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.lens.webForm

@Suppress("TooManyFunctions")
object GeneralWebLenses {

    fun userLens(contexts: RequestContexts): RequestContextLens<User?> =
        RequestContextKey
            .optional(contexts, "user")

    /**
     * Lens for getting {id} from request path
     */
    val idFromPathField = Path.int().of("id")
    val resultTypeFromPathField = Path.string().of("resultType")
    val checkIdFromPathField = Path.int().of("checkId")
    val pageCountFromPathField = Path.int().of("page")

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

    fun Request.resultTypeOrNull() = lensOrNull(resultTypeFromPathField, this)

    fun Request.checkIdOrNull() = lensOrNull(checkIdFromPathField, this)

    fun Request.pageCountOrNull() = lensOrNull(pageCountFromPathField, this)
}
