package checkme.db.users

import checkme.db.TestcontainerSpec
import checkme.db.appConfiguredPasswordHasher
import checkme.db.validName
import checkme.db.validPass
import checkme.db.validSurname
import checkme.domain.accounts.Role
import checkme.domain.models.User
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InsertUserTest : TestcontainerSpec({ context ->
    val userOperations = UserOperations(context)

    for (role in Role.entries) {
        test("Valid user with ${role}c an be inserted") {
            userOperations
                .insertUser(
                    validName,
                    validSurname,
                    validPass,
                    role
                ).shouldNotBeNull()
        }
    }

    test("Valid user insertion should return this user") {
        val insertedUser =
            userOperations
                .insertUser(
                    validName,
                    validSurname,
                    appConfiguredPasswordHasher.hash(validPass),
                    Role.STUDENT
                ).shouldNotBeNull()

        insertedUser.name.shouldBe(validName)
        insertedUser.surname.shouldBe(validSurname)
        insertedUser.password.shouldBe(appConfiguredPasswordHasher.hash(validPass))
        insertedUser.role.shouldBe(Role.STUDENT)
    }

    test("Valid user with long name can be inserted") {
        val insertedUser =
            userOperations
                .insertUser(
                    "а".repeat(User.MAX_NAME_AND_SURNAME_LENGTH),
                    "о".repeat(User.MAX_NAME_AND_SURNAME_LENGTH),
                    appConfiguredPasswordHasher.hash(validPass),
                    Role.STUDENT,
                ).shouldNotBeNull()

        insertedUser.name.shouldBe("а".repeat(User.MAX_NAME_AND_SURNAME_LENGTH))
        insertedUser.surname.shouldBe("о".repeat(User.MAX_NAME_AND_SURNAME_LENGTH))
        insertedUser.password.shouldBe(appConfiguredPasswordHasher.hash(validPass))
        insertedUser.role.shouldBe(Role.STUDENT)
    }
})
