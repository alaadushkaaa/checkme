package checkme.domain.operations.users

import checkme.db.*
import checkme.domain.accounts.Role
import checkme.domain.models.User
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.style.FunSpec

class CreateUserTest : FunSpec({
    val users = mutableListOf<User>()

    beforeEach {
        users.clear()
    }

    val insertUserMock: (
        login: String,
        name: String,
        surname: String,
        password: String,
        role: Role,
    ) -> User? = { login, name, surname, password, role ->
        val user =
            User(
                id = users.size + 1,
                login = login,
                name = name,
                surname = surname,
                password = password,
                role = role
            )
        users.add(user)
        user
    }

    val fetchUserByLoginMock: (String) -> User? = { userLogin ->
        users.firstOrNull { it.login == userLogin }
    }
    val insertUserNullMock: (
        login: String,
        name: String,
        surname: String,
        password: String,
        role: Role,
    ) -> User? = { _, _, _, _, _ -> null }

    val createUser = CreateUser(insertUserMock, fetchUserByLoginMock, config)
    val createUserNull = CreateUser(insertUserNullMock, fetchUserByLoginMock, config)

    test("Valid user can be inserted") {
        createUser(
            login = validLogin,
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole
        ).shouldBeSuccess()
    }

    listOf(
        validRole,
        validAdminRole
    ).forEach { role ->
        test("All valid roles can be inserted ($role)") {
            createUser(
                login = validLogin,
                name = validName,
                surname = validSurname,
                password = validPassword,
                role = role
            ).shouldBeSuccess()
        }
    }

    listOf(
        "",
        "     "
    ).forEach { invalidLogin ->
        test("User with invalid login should not be inserted ($invalidLogin)") {
            createUser(
                login = invalidLogin,
                name = validName,
                surname = validSurname,
                password = validPassword,
                role = validRole
            ).shouldBeFailure(UserCreationError.LOGIN_IS_BLANK_OR_EMPTY)
        }
    }

    test("User with invalid long login should not be inserted)") {
        createUser(
            login = "TooManyCharacters".repeat(User.MAX_LENGTH + 1),
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole
        ).shouldBeFailure(UserCreationError.LOGIN_IS_TOO_LONG)
    }

    test("User with invalid pattern login should not be inserted)") {
        createUser(
            login = "Некорректный логин",
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole
        ).shouldBeFailure(UserCreationError.LOGIN_PATTERN_MISMATCH)
    }

    listOf(
        "",
        "     "
    ).forEach { invalidName ->
        test("User with invalid name should not be inserted ($invalidName)") {
            createUser(
                login = validLogin,
                name = invalidName,
                surname = validSurname,
                password = validPassword,
                role = validRole
            ).shouldBeFailure(UserCreationError.NAME_IS_BLANK_OR_EMPTY)
        }
    }

    test("User with invalid long name should not be inserted)") {
        createUser(
            login = validLogin,
            name = "Много символов".repeat(User.MAX_LENGTH + 1),
            surname = validSurname,
            password = validPassword,
            role = validRole
        ).shouldBeFailure(UserCreationError.NAME_IS_TOO_LONG)
    }

    test("User with invalid pattern name should not be inserted)") {
        createUser(
            login = validPassword,
            name = "Invalid name",
            surname = validSurname,
            password = validPassword,
            role = validRole
        ).shouldBeFailure(UserCreationError.NAME_PATTERN_MISMATCH)
    }

    listOf(
        "",
        "     "
    ).forEach { invalidName ->
        test("User with invalid surname should not be inserted ($invalidName)") {
            createUser(
                login = validLogin,
                name = validName,
                surname = invalidName,
                password = validPassword,
                role = validRole
            ).shouldBeFailure(UserCreationError.SURNAME_IS_BLANK_OR_EMPTY)
        }
    }

    test("User with invalid long surname should not be inserted)") {
        createUser(
            login = validLogin,
            name = validName,
            surname = "Много символов".repeat(User.MAX_LENGTH + 1),
            password = validPassword,
            role = validRole
        ).shouldBeFailure(UserCreationError.SURNAME_IS_TOO_LONG)
    }

    test("User with invalid pattern surname should not be inserted)") {
        createUser(
            login = validPassword,
            name = validName,
            surname = "Invalid surname",
            password = validPassword,
            role = validRole
        ).shouldBeFailure(UserCreationError.SURNAME_PATTERN_MISMATCH)
    }

    test("User with invalid password should not be inserted") {
        createUser(
            login = validLogin,
            name = validName,
            surname = validSurname,
            password = "",
            role = validRole
        ).shouldBeFailure(UserCreationError.PASSWORD_IS_BLANK_OR_EMPTY)
    }

    test("There cannot be two users with the same login") {
        createUser(
            login = validLogin,
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole
        ).shouldBeSuccess()

        createUser(
            login = validLogin,
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole
        ).shouldBeFailure(UserCreationError.LOGIN_ALREADY_EXISTS)
    }

    test("Unknown db error test for CreateUser") {
        createUserNull(
            login = validLogin,
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole
        ).shouldBeFailure(UserCreationError.UNKNOWN_DATABASE_ERROR)
    }
})
