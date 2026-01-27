package checkme.domain.models

import checkme.db.validName
import checkme.db.validPassword
import checkme.db.validRole
import checkme.db.validSurname
import checkme.db.validUserId
import checkme.domain.accounts.Role
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UserTest : FunSpec({
    val validUsers = listOf(
        User(
            id = validUserId[0],
            login = "user1",
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole,
        ),
        User(
            id = validUserId[1],
            login = "admin",
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = Role.ADMIN,
        )
    )

    val validUser = validUsers.first()

    val namesWithInvalidSymbols = listOf("Incorrect name", "Имя 12345")
    val emptyNames = listOf( "", "  ")
    val surnamesWithInvalidSymbols = listOf("Incorrect surname", "Фамилия 12345")
    val emptySurnames = listOf("", "  ")
    val loginsWithInvalidSymbols = listOf("Некорректный логин", "In;'&&??-1.0")
    val emptyLogins = listOf("", "  ")
    val emptyPasses = listOf("", "  ")
    val longValidName = "А".repeat(User.MAX_LENGTH)
    val longInvalidName = "Длинное имя".repeat(User.MAX_LENGTH)
    val longValidSurname = "А".repeat(User.MAX_LENGTH)
    val longInvalidSurname = "Длинная фамилия".repeat(User.MAX_LENGTH)
    val longValidLogin = "a".repeat(User.MAX_LENGTH)
    val longInvalidLogin = "TooManyCharacters".repeat(User.MAX_LENGTH + 1)

    validUsers.forEach { user ->
        test("Should return  ValidateUserResult.ALL_OK when userdata is $user") {
            User.validateUser(
                login = user.login,
                name = user.name,
                surname = user.surname,
                password = user.password
            ) shouldBe ValidateUserResult.ALL_OK
        }
    }

    loginsWithInvalidSymbols.forEach { login ->
        test("Should return ValidateUserResult.LOGIN_PATTERN_MISMATCH when login is $login") {
            User.validateUser(
                login = login,
                name = validUser.name,
                surname = validUser.surname,
                password = validUser.password
            ) shouldBe ValidateUserResult.LOGIN_PATTERN_MISMATCH
        }
    }

    namesWithInvalidSymbols.forEach { name ->
        test("Should return ValidateUserResult.NAME_PATTERN_MISMATCH when name is $name") {
            User.validateUser(
                login = validUser.login,
                name = name,
                surname = validUser.surname,
                password = validUser.password
            ) shouldBe ValidateUserResult.NAME_PATTERN_MISMATCH
        }
    }

    surnamesWithInvalidSymbols.forEach { surname ->
        test("Should return ValidateUserResult.SURNAME_PATTERN_MISMATCH when surname is $surname") {
            User.validateUser(
                login = validUser.login,
                name = validUser.name,
                surname = surname,
                password = validUser.password
            ) shouldBe ValidateUserResult.SURNAME_PATTERN_MISMATCH
        }
    }

    emptyLogins.forEach { login ->
        test("Should return ValidateUserResult.LOGIN_IS_BLANK_OR_EMPTY when login is $login") {
            User.validateUser(
                login = login,
                name = validUser.name,
                surname = validUser.surname,
                password = validUser.password
            ) shouldBe ValidateUserResult.LOGIN_IS_BLANK_OR_EMPTY
        }
    }

    emptyNames.forEach { name ->
        test("Should return ValidateUserResult.NAME_IS_BLANK_OR_EMPTY when name is $name") {
            User.validateUser(
                login = validUser.login,
                name = name,
                surname = validUser.surname,
                password = validUser.password
            ) shouldBe ValidateUserResult.NAME_IS_BLANK_OR_EMPTY
        }
    }

    emptySurnames.forEach { surname ->
        test("Should return ValidateUserResult.NAME_IS_BLANK_OR_EMPTY when surname is $surname") {
            User.validateUser(
                login = validUser.login,
                name = validUser.name,
                surname = surname,
                password = validUser.password
            ) shouldBe ValidateUserResult.SURNAME_IS_BLANK_OR_EMPTY
        }
    }

    emptyPasses.forEach { password ->
        test("Should return ValidateUserResult.PASSWORD_IS_BLANK_OR_EMPTY when pass is $password") {
            User.validateUser(
                login = validUser.login,
                name = validUser.name,
                surname = validUser.surname,
                password = password
            ) shouldBe ValidateUserResult.PASSWORD_IS_BLANK_OR_EMPTY
        }
    }

    test("Should return ValidateUserResult.LOGIN_IS_TOO_LONG when login is longer than MAX_LENGTH") {
        User.validateUser(
            login = longInvalidLogin,
            name = validUser.name,
            surname = validUser.surname,
            password = validUser.password
        ) shouldBe ValidateUserResult.LOGIN_IS_TOO_LONG
    }

    test("Should return ValidateUserResult.ALL_OK when login length is MAX_LENGTH") {
        User.validateUser(
            login = longValidLogin,
            name = validUser.name,
            surname = validUser.surname,
            password = validUser.password
        ) shouldBe ValidateUserResult.ALL_OK
    }

    test("Should return ValidateUserResult.NAME_IS_TOO_LONG when name is longer than MAX_LENGTH") {
        User.validateUser(
            login = validUser.login,
            name = longInvalidName,
            surname = validUser.surname,
            password = validUser.password
        ) shouldBe ValidateUserResult.NAME_IS_TOO_LONG
    }

    test("Should return ValidateUserResult.ALL_OK when name length is MAX_LENGTH") {
        User.validateUser(
            login = validUser.login,
            name = longValidName,
            surname = validUser.surname,
            password = validUser.password
        ) shouldBe ValidateUserResult.ALL_OK
    }

    test("Should return ValidateUserResult.SURNAME_IS_TOO_LONG when surname is longer than MAX_LENGTH") {
        User.validateUser(
            login = validUser.login,
            name = validUser.name,
            surname = longInvalidSurname,
            password = validUser.password
        ) shouldBe ValidateUserResult.SURNAME_IS_TOO_LONG
    }

    test("Should return ValidateUserResult.ALL_OK when surname length is MAX_LENGTH") {
        User.validateUser(
            login = validUser.login,
            name = validUser.name,
            surname = longValidSurname,
            password = validUser.password
        ) shouldBe ValidateUserResult.ALL_OK
    }
})
