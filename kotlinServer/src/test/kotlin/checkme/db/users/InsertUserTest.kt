package checkme.db.users

import checkme.db.TestcontainerSpec
import checkme.db.appConfiguredPasswordHasher
import checkme.db.validLogin
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
        test("Valid user with $role can be inserted") {
            userOperations
                .insertUser(
                    validLogin,
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
                    validLogin,
                    validName,
                    validSurname,
                    appConfiguredPasswordHasher.hash(validPass),
                    Role.STUDENT
                ).shouldNotBeNull()

        insertedUser.login.shouldBe(validLogin)
        insertedUser.name.shouldBe(validName)
        insertedUser.surname.shouldBe(validSurname)
        insertedUser.password.shouldBe(appConfiguredPasswordHasher.hash(validPass))
        insertedUser.role.shouldBe(Role.STUDENT)
    }

    test("Valid user with long name can be inserted") {
        val insertedUser =
            userOperations
                .insertUser(
                    validLogin,
                    "а".repeat(User.MAX_LENGTH),
                    validSurname,
                    appConfiguredPasswordHasher.hash(validPass),
                    Role.STUDENT,
                ).shouldNotBeNull()

        insertedUser.login.shouldBe(validLogin)
        insertedUser.name.shouldBe("а".repeat(User.MAX_LENGTH))
        insertedUser.surname.shouldBe(validSurname)
        insertedUser.password.shouldBe(appConfiguredPasswordHasher.hash(validPass))
        insertedUser.role.shouldBe(Role.STUDENT)
    }

    test("Valid user with long login can be inserted") {
        val insertedUser =
            userOperations
                .insertUser(
                    "а".repeat(User.MAX_LENGTH),
                    validName,
                    validSurname,
                    appConfiguredPasswordHasher.hash(validPass),
                    Role.STUDENT,
                ).shouldNotBeNull()

        insertedUser.login.shouldBe("а".repeat(User.MAX_LENGTH))
        insertedUser.name.shouldBe(validName)
        insertedUser.surname.shouldBe(validSurname)
        insertedUser.password.shouldBe(appConfiguredPasswordHasher.hash(validPass))
        insertedUser.role.shouldBe(Role.STUDENT)
    }

    test("Valid user with long surname can be inserted") {
        val insertedUser =
            userOperations
                .insertUser(
                    validLogin,
                    validName,
                    "о".repeat(User.MAX_LENGTH),
                    appConfiguredPasswordHasher.hash(validPass),
                    Role.STUDENT,
                ).shouldNotBeNull()

        insertedUser.login.shouldBe(validLogin)
        insertedUser.name.shouldBe(validName)
        insertedUser.surname.shouldBe("о".repeat(User.MAX_LENGTH))
        insertedUser.password.shouldBe(appConfiguredPasswordHasher.hash(validPass))
        insertedUser.role.shouldBe(Role.STUDENT)
    }
})
