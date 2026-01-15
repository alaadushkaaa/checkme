package checkme.domain.models

import checkme.domain.accounts.Role
import java.util.UUID

data class User(
    val id: UUID,
    val login: String,
    val name: String,
    val surname: String,
    val password: String,
    val role: Role,
) {
    companion object {
        fun validateUser(
            login: String,
            name: String,
            surname: String,
            password: String,
        ): ValidateUserResult =
            when {
                login.isBlank() -> ValidateUserResult.LOGIN_IS_BLANK_OR_EMPTY
                login.length > MAX_LENGTH -> ValidateUserResult.LOGIN_IS_TOO_LONG
                !loginPattern.matches(login) -> ValidateUserResult.LOGIN_PATTERN_MISMATCH
                name.isBlank() -> ValidateUserResult.NAME_IS_BLANK_OR_EMPTY
                name.length > MAX_LENGTH -> ValidateUserResult.NAME_IS_TOO_LONG
                !namePattern.matches(name) -> ValidateUserResult.NAME_PATTERN_MISMATCH
                surname.isBlank() -> ValidateUserResult.SURNAME_IS_BLANK_OR_EMPTY
                surname.length > MAX_LENGTH -> ValidateUserResult.SURNAME_IS_TOO_LONG
                !namePattern.matches(surname) -> ValidateUserResult.SURNAME_PATTERN_MISMATCH
                password.isBlank() -> ValidateUserResult.PASSWORD_IS_BLANK_OR_EMPTY
                else -> ValidateUserResult.ALL_OK
            }

        const val MAX_LENGTH = 30

        val namePattern = Regex("^[А-Яа-я -]+\$")
        val loginPattern = Regex("^[\\w-.]+\$")
    }

    fun isStudent() = role == Role.STUDENT

    fun isAdmin() = role == Role.ADMIN
}

enum class ValidateUserResult {
    NAME_IS_BLANK_OR_EMPTY,
    NAME_IS_TOO_LONG,
    NAME_PATTERN_MISMATCH,
    LOGIN_IS_BLANK_OR_EMPTY,
    LOGIN_IS_TOO_LONG,
    LOGIN_PATTERN_MISMATCH,
    SURNAME_IS_BLANK_OR_EMPTY,
    SURNAME_IS_TOO_LONG,
    SURNAME_PATTERN_MISMATCH,
    PASSWORD_IS_BLANK_OR_EMPTY,
    ALL_OK,
}
