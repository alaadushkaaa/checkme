package checkme.web.auth.handlers

import checkme.config.AuthConfig
import checkme.domain.models.User
import checkme.domain.models.ValidateUserEmailResult
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.auth.forms.SignUpRequest
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.lenses.RegistrationStudentsLenses
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.RequestContextLens
import java.io.InputStream
import kotlin.text.matches

class LoadStudentsHandler(
    private val userLens: RequestContextLens<User?>,
    private val userOperations: UserOperationHolder,
    private val config: AuthConfig,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = userLens(request)
        val objectMapper = jacksonObjectMapper()
        val form: MultipartForm = RegistrationStudentsLenses.formField(request)
        val fileForRegistration = RegistrationStudentsLenses.fileField(form)

        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(RegistrationsStudentsError.USER_HAS_NOT_RIGHTS.errorText)

            fileForRegistration.contentType != ContentType.TEXT_CSV ->
                objectMapper.sendBadRequestError(RegistrationsStudentsError.NOT_SUPPORTED_FILE_CONTENT_TYPE.errorText)

            else -> when (val studentsFromCsv = extractStudentsData(fileForRegistration)) {
                is Failure -> objectMapper.sendBadRequestError(studentsFromCsv.reason.errorText)

                is Success -> sendLoadUsersResponse()
            }
        }
    }
}

private fun extractStudentsData(
    file: MultipartFormFile
): Result4k<List<SignUpRequest>, RegistrationsStudentsError> {
    val csvFileStream = file.content
    return when (val studentsData = loadStudentsFromCSV(csvFileStream)) {
        is Success -> Success(studentsData.value)

        is Failure -> Failure(studentsData.reason)
    }

}

private fun loadStudentsFromCSV(
    csvFileStream: InputStream,
): Result4k<List<SignUpRequest>, RegistrationsStudentsError> {
    val students = mutableListOf<SignUpRequest>()
    return csvFileStream.bufferedReader().use { reader ->
        reader.forEachLine { line ->
            if (line.isNotBlank()) {
                val parts = line.split(',')
                    .map { it.trim().removeSurrounding("\"") }
                when {
                    parts.size == 3 -> {
                        val firstName = parts[0]
                        val lastName = parts[1]
                        val email = parts[2]

                        when {
                            !firstName.matches(User.namePattern) ->
                                Failure(RegistrationsStudentsError.INCORRECT_USER_NAME)

                            !lastName.matches(User.namePattern) ->
                                Failure(RegistrationsStudentsError.INCORRECT_USER_SURNAME)

                            User.validateUserDataEmail(email) != ValidateUserEmailResult.ALL_OK ->
                                Failure(RegistrationsStudentsError.INCORRECT_USER_EMAIL)

                            else -> {
                                val username = email.substringBefore("@")

                                students.add(
                                    SignUpRequest(
                                        username = username,
                                        name = firstName,
                                        surname = lastName,
                                        password = email
                                    )
                                )
                            }
                        }
                    }

                    else -> Failure(RegistrationsStudentsError.INCORRECT_FILE_DATA)
                }
            }
        }
        Success(students.distinct())
    }
}

private fun insertLoadedUsers(
    users: List<SignUpRequest>
) {
    
}

enum class RegistrationsStudentsError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    USER_HAS_NOT_RIGHTS("Not allowed to registration students"),
    NOT_SUPPORTED_FILE_CONTENT_TYPE("File with this content type is not supported for students registration"),
    NO_SUCH_USER("Can not find user"),
    IS_NOT_STUDENT("This user is not a student"),
    INCORRECT_USER_NAME("Incorrect user name"),
    INCORRECT_USER_SURNAME("Incorrect user surname"),
    INCORRECT_USER_EMAIL("User email is incorrect"),
    INCORRECT_FILE_DATA("Incorrect data in file")
}
