package checkme.web.admin.handlers

import checkme.domain.models.User
import checkme.domain.models.ValidateUserEmailResult
import checkme.domain.operations.users.UserOperationHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.admin.suppirtingFiles.DataAndPreviousDataSize
import checkme.web.auth.supportingFiles.PasswordGenerator
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.lenses.RegistrationStudentsLenses
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.RequestContextLens
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import kotlin.math.log

const val DATA_FOR_PASSWORDS_SIZE = 1

class LoadSystemPasswordsHandler(
    private val userLens: RequestContextLens<User?>,
    private val userOperations: UserOperationHolder,
    private val passwordGenerator: PasswordGenerator,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = userLens(request)
        val objectMapper = jacksonObjectMapper()
        val form = RegistrationStudentsLenses.formField(request)
        val fileWithStudentData = RegistrationStudentsLenses.fileField(form)

        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(LoadingPasswordsError.USER_HAS_NOT_RIGHTS)

            fileWithStudentData.contentType != ContentType.TEXT_CSV ->
                objectMapper.sendBadRequestError(LoadingPasswordsError.NOT_SUPPORTED_FILE_CONTENT_TYPE.errorText)

            else -> {
                ServerLogger.log(
                    user = user,
                    action = "Loading users system passwords",
                    message = "Start loading",
                    type = LoggerType.INFO
                )
                val studentData = extractStudentsLogins(
                    file = fileWithStudentData,
                    user = user
                )
                val studentsWithSystemPasswords = fetchUsersAndGetSystemPasswords(
                    emailsAndLogins = studentData.emailAndLogins,
                    user = user
                )
                if (studentData.previousSize != studentsWithSystemPasswords.size) {
                    return objectMapper.sendBadRequestError(LoadingPasswordsError.FILE_CONTAINS_MISTAKES.errorText)
                }
                val csvContent = createCsvFile(
                    studentsWithPasswords = studentsWithSystemPasswords
                )
                Response(Status.OK)
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"system-passwords.csv\"")
                    .body(csvContent)
            }
        }
    }

    private fun extractStudentsLogins(
        file: MultipartFormFile,
        user: User,
    ): DataAndPreviousDataSize {
        val csvStream = file.content
        return getLoginsFromFile(
            user = user,
            fileStream = csvStream
        )
    }

    private fun getLoginsFromFile(
        user: User,
        fileStream: InputStream,
    ): DataAndPreviousDataSize {
        val emailsAndLogins = mutableListOf<Pair<String, String>>()
        val errors = mutableMapOf<String, String>()
        var countLines = 0
        return InputStreamReader(fileStream, Charsets.UTF_8).use { reader ->
            runCatching {
                val csvParser = CSVParser.parse(reader, CSVFormat.DEFAULT)
                val lines = csvParser.records
                countLines = lines.size
                lines.forEachIndexed { index, line ->
                    when {
                        line.size() == DATA_FOR_PASSWORDS_SIZE -> validateLine(
                            line = line,
                            errors = errors,
                            logins = emailsAndLogins
                        )

                        else -> errors.put(
                            "Номер строки ${index + 1}",
                            LoadingPasswordsError.INCORRECT_FILE_DATA.errorText
                        )
                    }
                }
            }.onFailure {
                ServerLogger.log(
                    user = user,
                    action = "Load users passwords errors",
                    message = "Error while trying to parse csv file. Error: ${it.message}",
                    type = LoggerType.WARN
                )
            }

            for (error in errors) ServerLogger.log(
                user = user,
                action = "Load users passwords errors",
                message = "Something was wrong while trying to get users system passwords. ${error.key} " +
                    "- ${error.value}",
                type = LoggerType.WARN
            )
            DataAndPreviousDataSize(
                emailAndLogins = emailsAndLogins,
                previousSize = countLines
            )
        }
    }

    private fun validateLine(
        line: CSVRecord,
        errors: MutableMap<String, String>,
        logins: MutableList<Pair<String, String>>,
    ) {
        val email = line.get(0).trim()

        when {
            User.validateUserDataEmail(email) != ValidateUserEmailResult.ALL_OK ->
                errors.put(email, LoadingPasswordsError.INCORRECT_USER_EMAIL.errorText)

            else -> {
                val login = email.substringBefore("@")
                if (logins.contains(Pair(email, login))) {
                    errors.put(
                        email,
                        LoadingPasswordsError.DUPLICATE_EMAILS.errorText
                    )
                } else {
                    logins.add(Pair(email, login))
                }
            }
        }
    }

    private fun fetchUsersAndGetSystemPasswords(
        emailsAndLogins: List<Pair<String, String>>,
        user: User,
    ): Map<String, Pair<String, String>> {
        val dataWithPasswords = mutableMapOf<String, Pair<String, String>>()
        for (data in emailsAndLogins) {
            if (fetchUserByLogin(data.second)) {
                dataWithPasswords.put(
                    data.first,
                    Pair(
                        data.second,
                        passwordGenerator.generateStudentPass(data.first)
                    )
                )
            } else {
                ServerLogger.log(
                    user = user,
                    action = "Load users passwords errors",
                    message = "User with email ${data.first} does not exist in the system",
                    type = LoggerType.WARN
                )
            }
        }
        return dataWithPasswords
    }

    private fun fetchUserByLogin(login: String): Boolean {
        return when (userOperations.fetchUserByLogin(login)) {
            is Failure -> false
            is Success -> true
        }
    }

    private fun createCsvFile(studentsWithPasswords: Map<String, Pair<String, String>>): String {
        val writer = StringWriter()
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
        csvPrinter.printRecord("Email", "Login", "System password")

        studentsWithPasswords.forEach { data ->
            val email = data.key
            val login = data.value.first
            val pass = data.value.second
            csvPrinter.printRecord(email, login, pass)
        }
        csvPrinter.flush()
        csvPrinter.close()
        return writer.toString()
    }
}

enum class LoadingPasswordsError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    USER_HAS_NOT_RIGHTS("Not allowed to load students passwords"),
    NOT_SUPPORTED_FILE_CONTENT_TYPE("File with this content type is not supported for load students passwords"),
    INCORRECT_FILE_DATA("Incorrect data in file"),
    INCORRECT_USER_EMAIL("User email is incorrect"),
    DUPLICATE_EMAILS("File contains duplicated emails"),
    FILE_CONTAINS_MISTAKES(
        "File for get students system passwords contains mistakes. Please, see log file and correct it"
    ),
}
