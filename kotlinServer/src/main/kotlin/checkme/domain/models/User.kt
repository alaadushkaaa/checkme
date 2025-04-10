package checkme.domain.models

import checkme.domain.accounts.Role

data class User(
    val id: Int,
    val name: String,
    val surname: String,
    val password: String,
    val role: Role,
) {
    companion object {
        fun validateUser(
            name: String,
            surname: String,
            password: String,
        ): ValidateUserResult =
            when {
                name.isBlank() -> ValidateUserResult.NAME_IS_BLANK_OR_EMPTY
                name.length > MAX_NAME_AND_SURNAME_LENGTH -> ValidateUserResult.NAME_IS_TOO_LONG
                !namePattern.matches(name) -> ValidateUserResult.NAME_PATTERN_MISMATCH
                surname.isBlank() -> ValidateUserResult.SURNAME_IS_BLANK_OR_EMPTY
                surname.length > MAX_NAME_AND_SURNAME_LENGTH -> ValidateUserResult.SURNAME_IS_TOO_LONG
                !surnamePattern.matches(surname) -> ValidateUserResult.SURNAME_PATTERN_MISMATCH
                password.isBlank() -> ValidateUserResult.PASSWORD_IS_BLANK_OR_EMPTY
                else -> ValidateUserResult.ALL_OK
            }

        const val MAX_NAME_AND_SURNAME_LENGTH = 50

        val namePattern = Regex("^[\\w-.]+\$")
        val surnamePattern = Regex("^[\\w-.]+\$")
    }

    fun isStudent() = role == Role.STUDENT

    fun isAdmin() = role == Role.ADMIN
}

enum class ValidateUserResult {
    NAME_IS_BLANK_OR_EMPTY,
    NAME_IS_TOO_LONG,
    NAME_PATTERN_MISMATCH,
    SURNAME_IS_BLANK_OR_EMPTY,
    SURNAME_IS_TOO_LONG,
    SURNAME_PATTERN_MISMATCH,
    PASSWORD_IS_BLANK_OR_EMPTY,
    ALL_OK,
}
