package checkme.web.auth.handlers

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.models.ValidateUserEmailResult
import checkme.domain.operations.users.UserFetchingError
import checkme.domain.operations.users.UserOperationHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.auth.forms.SignUpRequest
import checkme.web.auth.supportingFiles.PasswordGenerator
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.RegistrationStudentsLenses
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.RequestContextLens
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.collections.contains
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

            else -> {
                ServerLogger.log(
                    user = user,
                    action = "Automatic user registration",
                    message = "Starting registration",
                    type = LoggerType.INFO
                )
                val studentsFromCsv = extractStudentsData(fileForRegistration, user)
                val insertedUsers = insertLoadedUsers(studentsFromCsv.second, userOperations, user)
                sendLoadUsersResponse(
                    studentsFromCsv.first,
                    insertedUsers = insertedUsers,
                    objectMapper = objectMapper
                )
            }
        }
    }

    private fun extractStudentsData(
        file: MultipartFormFile,
        user: User,
    ): Pair<Int, Map<String, SignUpRequest>> {
        val csvFileStream = file.content
        return loadStudentsCSV(csvFileStream, user)
    }

    private fun loadStudentsCSV(
        fileStream: InputStream,
        user: User,
    ): Pair<Int, Map<String, SignUpRequest>> {
        val students = mutableMapOf<String, SignUpRequest>()
        val errors = mutableMapOf<String, String>()
        var countLines = 0
        return InputStreamReader(fileStream, Charsets.UTF_8).use { reader ->
            runCatching {
                val csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT)
                val lines = csvParser.records
                countLines = lines.size
                lines.forEachIndexed { index, line ->
                    when {
                        line.size() == STUDENT_DATA_SIZE -> validateLineData(
                            line = line,
                            errors = errors,
                            students = students
                        )

                        else -> errors.put("Номер строки ${index + 1}", RegistrationError.INCORRECT_FILE_DATA.errorText)
                    }
                }
            }.onFailure {
                ServerLogger.log(
                    user = user,
                    action = "Automatic user registration errors",
                    message = "Error while trying to parse csv file. Error: ${it.message}",
                    type = LoggerType.WARN
                )
            }
            for (error in errors) ServerLogger.log(
                user = user,
                action = "Automatic user registration errors",
                message = "Something was wrong while trying to register a user automatically. ${error.key} " +
                    "- ${error.value}",
                type = LoggerType.WARN
            )
            Pair(countLines, students)
        }
    }

    private fun validateLineData(
        line: CSVRecord,
        errors: MutableMap<String, String>,
        students: MutableMap<String, SignUpRequest>,
    ) {
        val firstName = line.get(0).trim()
        val lastName = line.get(1).trim()
        val email = line.get(2).trim()

        when {
            !firstName.matches(User.namePattern) ->
                errors.put(email, RegistrationError.INCORRECT_USER_NAME.errorText)

            !lastName.matches(User.namePattern) ->
                errors.put(email, RegistrationError.INCORRECT_USER_SURNAME.errorText)

            User.validateUserDataEmail(email) != ValidateUserEmailResult.ALL_OK ->
                errors.put(email, RegistrationError.INCORRECT_USER_EMAIL.errorText)

            else -> {
                if (students.contains(email)) {
                    errors.put(
                        email,
                        RegistrationError.DUPLICATE_EMAILS.errorText
                    )
                } else {
                    students.put(
                        email,
                        SignUpRequest(
                            username = email.substringBefore("@"),
                            name = firstName,
                            surname = lastName,
                            password = passwordGenerator.generateStudentPass(email)
                        )
                    )
                }
            }
        }
    }

    private fun insertLoadedUsers(
        users: Map<String, SignUpRequest>,
        userOperations: UserOperationHolder,
        user: User,
    ): Map<String, Pair<String, String>> =
        users.mapNotNull { userData ->
            tryAddUser(
                userData = userData.value,
                userOperations = userOperations,
                user = user
            ).valueOrNull()?.let { userData.key to Pair(userData.value.name, userData.value.surname) }
        }.toMap()

    private fun tryAddUser(
        userData: SignUpRequest,
        userOperations: UserOperationHolder,
        user: User,
    ): Result<User, RegistrationError> {
        return when (val fetchingUser = userOperations.fetchUserByLogin(userData.username)) {
            is Failure -> when (fetchingUser.reason) {
                UserFetchingError.UNKNOWN_DATABASE_ERROR -> {
                    ServerLogger.log(
                        user = user,
                        action = "Automatic user registration errors",
                        message = "Unknown database error while trying to register a user automatically.",
                        type = LoggerType.WARN
                    )
                    Failure(RegistrationError.UNKNOWN_DATABASE_ERROR)
                }

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
                        is Failure -> {
                            ServerLogger.log(
                                user = user,
                                action = "Automatic user registration errors",
                                message = "Unknown database error while trying to register a user automatically.",
                                type = LoggerType.WARN
                            )
                            Failure(RegistrationError.UNKNOWN_DATABASE_ERROR)
                        }
                    }
                }
            }

            is Success -> {
                ServerLogger.log(
                    user = user,
                    action = "Automatic user registration errors",
                    message = "Error while trying to register a user automatically. User ${userData.username} " +
                        "already exists",
                    type = LoggerType.WARN
                )
                Failure(RegistrationError.USER_ALREADY_EXISTS)
            }
        }
    }

    private fun sendLoadUsersResponse(
        usersListSize: Int,
        insertedUsers: Map<String, Pair<String, String>>,
        objectMapper: ObjectMapper,
    ): Response {
        return if (insertedUsers.isEmpty()) {
            objectMapper.sendBadRequestError(RegistrationError.NO_USERS_REGISTERED.errorText)
        } else if (usersListSize > insertedUsers.size) {
            objectMapper.sendBadRequestError(RegistrationError.NOT_ALL_USERS_REGISTERED.errorText)
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
    NOT_ALL_USERS_REGISTERED("Not all students were registered. See the journal for details."),
    INCORRECT_USER_NAME("Incorrect user name"),
    INCORRECT_USER_SURNAME("Incorrect user surname"),
    INCORRECT_USER_EMAIL("User email is incorrect"),
    INCORRECT_FILE_DATA("Incorrect data in file"),
}
