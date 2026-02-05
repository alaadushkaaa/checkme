package checkme.web.auth.handlers

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.models.ValidateUserEmailResult
import checkme.domain.operations.users.UserFetchingError
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.auth.forms.SignUpRequest
import checkme.web.auth.supportingFiles.PasswordGenerator
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.RegistrationStudentsLenses
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.failureOrNull
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.RequestContextLens
import java.io.InputStream
import kotlin.text.matches

const val STUDENT_DATA_SIZE = 3

class LoadStudentsHandler(
    private val userLens: RequestContextLens<User?>,
    private val userOperations: UserOperationHolder,
    private val passwordGenerator: PasswordGenerator,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = userLens(request)
        val objectMapper = jacksonObjectMapper()
        val form: MultipartForm = RegistrationStudentsLenses.formField(request)
        val fileForRegistration = RegistrationStudentsLenses.fileField(form)

        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(RegistrationError.USER_HAS_NOT_RIGHTS.errorText)

            fileForRegistration.contentType != ContentType.TEXT_CSV ->
                objectMapper.sendBadRequestError(RegistrationError.NOT_SUPPORTED_FILE_CONTENT_TYPE.errorText)

            else -> when (val studentsFromCsv = extractStudentsData(fileForRegistration)) {
                is Failure -> objectMapper.sendBadRequestError(studentsFromCsv.reason.errorText)

                is Success -> {
                    val insertedUsers = insertLoadedUsers(studentsFromCsv.value, userOperations)
                    sendLoadUsersResponse(
                        insertedUsers = insertedUsers,
                        objectMapper = objectMapper
                    )
                }
            }
        }
    }

    private fun extractStudentsData(file: MultipartFormFile): Result4k<Map<String, SignUpRequest>, RegistrationError> {
        val csvFileStream = file.content
        return when (val studentsData = loadStudentsCSV(csvFileStream)) {
            is Success -> Success(studentsData.value)

            is Failure -> Failure(studentsData.reason)
        }
    }

    private fun loadStudentsCSV(fileStream: InputStream): Result4k<Map<String, SignUpRequest>, RegistrationError> {
        val students = mutableMapOf<String, SignUpRequest>()
        return fileStream.bufferedReader().use { reader ->
            reader.forEachLine { line ->
                if (line.isNotBlank()) {
                    val parts = line.split(',')
                        .map { it.trim().removeSurrounding("\"") }
                    when {
                        parts.size == STUDENT_DATA_SIZE -> {
                            val firstName = parts[0]
                            val lastName = parts[1]
                            val email = parts[2]

                            when {
                                !firstName.matches(User.namePattern) ->
                                    Failure(RegistrationError.INCORRECT_USER_NAME)

                                !lastName.matches(User.namePattern) ->
                                    Failure(RegistrationError.INCORRECT_USER_SURNAME)

                                User.validateUserDataEmail(email) != ValidateUserEmailResult.ALL_OK ->
                                    Failure(RegistrationError.INCORRECT_USER_EMAIL)

                                else -> {
                                    if (students.contains(email)) Failure(RegistrationError.DUPLICATE_EMAILS)
                                    val username = email.substringBefore("@")
                                    students.put(
                                        email.substringBefore("@"),
                                        SignUpRequest(
                                            username = username,
                                            name = firstName,
                                            surname = lastName,
                                            password = passwordGenerator.generateStudentPass(email)
                                        )
                                    )
                                }
                            }
                        }

                        else -> Failure(RegistrationError.INCORRECT_FILE_DATA)
                    }
                }
            }
            Success(students)
        }
    }

    private fun insertLoadedUsers(
        users: Map<String, SignUpRequest>,
        userOperations: UserOperationHolder,
    ): Map<String, Pair<String, String>> =
        users.mapNotNull { userData ->
            tryAddUser(
                userData = userData.value,
                userOperations = userOperations
            ).failureOrNull()?.let { userData.key to Pair(userData.value.name, userData.value.surname) }
        }.toMap()

    private fun tryAddUser(
        userData: SignUpRequest,
        userOperations: UserOperationHolder,
    ): Result<User, RegistrationError> {
        return when (val fetchingUser = userOperations.fetchUserByLogin(userData.username)) {
            is Failure -> when (fetchingUser.reason) {
                UserFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(RegistrationError.UNKNOWN_DATABASE_ERROR)
                UserFetchingError.NO_SUCH_USER -> {
                    when (
                        val addedUser = userOperations.createUser(
                            userData.username,
                            userData.name,
                            userData.surname,
                            userData.password,
                            Role.STUDENT
                        )
                    ) {
                        is Success -> Success(addedUser.value)
                        is Failure -> Failure(RegistrationError.UNKNOWN_DATABASE_ERROR)
                    }
                }
            }

            is Success -> Failure(RegistrationError.USER_ALREADY_EXISTS)
        }
    }

    private fun sendLoadUsersResponse(
        insertedUsers: Map<String, Pair<String, String>>,
        objectMapper: ObjectMapper,
    ): Response {
        return if (insertedUsers.isEmpty()) {
            objectMapper.sendBadRequestError(RegistrationError.NO_USERS_REGISTERED.errorText)
        } else {
            objectMapper.sendOKResponse(insertedUsers)
        }
    }
}

enum class RegistrationError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    USER_HAS_NOT_RIGHTS("Not allowed to registration students"),
    NOT_SUPPORTED_FILE_CONTENT_TYPE("File with this content type is not supported for students registration"),
    DUPLICATE_EMAILS("File contains duplicated emails"),
    NO_USERS_REGISTERED("No users are registered automatically"),
    USER_ALREADY_EXISTS("This user already exists"),
    INCORRECT_USER_NAME("Incorrect user name"),
    INCORRECT_USER_SURNAME("Incorrect user surname"),
    INCORRECT_USER_EMAIL("User email is incorrect"),
    INCORRECT_FILE_DATA("Incorrect data in file"),
}
