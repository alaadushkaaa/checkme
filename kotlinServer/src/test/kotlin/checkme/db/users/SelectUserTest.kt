package checkme.db.users

import checkme.db.TestcontainerSpec
import checkme.db.appConfiguredPasswordHasher
import checkme.db.validAdminLogin
import checkme.db.validLogin
import checkme.db.validName
import checkme.db.validPass
import checkme.db.validSurname
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.web.solution.forms.UserDataForUsersList
import checkme.web.solution.forms.UserNameSurnameForAllResults
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SelectUserTest : TestcontainerSpec({ context ->
    val userOperations = UserOperations(context)
    lateinit var insertedUser: User
    lateinit var insertedAdmin: User

    beforeEach {
        insertedUser =
            userOperations
                .insertUser(
                    validLogin,
                    validName,
                    validSurname,
                    appConfiguredPasswordHasher.hash(validPass),
                    Role.STUDENT,
                ).shouldNotBeNull()

        insertedAdmin =
            userOperations
                .insertUser(
                    validAdminLogin,
                    validName,
                    validSurname,
                    appConfiguredPasswordHasher.hash(validPass),
                    Role.ADMIN,
                ).shouldNotBeNull()
    }

    test("Select all users from db") {

        userOperations
            .insertUser(
                validLogin + "1",
                validName,
                validSurname,
                appConfiguredPasswordHasher.hash(validPass + "2"),
                Role.STUDENT,
            ).shouldNotBeNull()

        userOperations
            .selectAllUsers()
            .shouldNotBeNull()
            .size
            .shouldBe(3)
    }

    test("Select students from db") {

        userOperations
            .insertUser(
                validLogin + "1",
                validName,
                validSurname,
                appConfiguredPasswordHasher.hash(validPass + "2"),
                Role.STUDENT,
            ).shouldNotBeNull()

        userOperations
            .selectUsersByRole(Role.STUDENT)
            .shouldNotBeNull()
            .size
            .shouldBe(2)
    }

    test("Select admin from db") {
        userOperations
            .selectUsersByRole(Role.ADMIN)
            .shouldNotBeNull()
            .size
            .shouldBe(1)
    }

    test("Fetch user by valid id") {
        val fetchedUser =
            userOperations
                .selectUserById(insertedUser.id)
                .shouldNotBeNull()

        fetchedUser.login.shouldBe(validLogin)
        fetchedUser.name.shouldBe(validName)
        fetchedUser.surname.shouldBe(validSurname)
        fetchedUser.password
            .shouldBe(appConfiguredPasswordHasher.hash(validPass))
        fetchedUser.role.shouldBe(Role.STUDENT)
        fetchedUser.id.shouldBe(insertedUser.id)
    }

    test("Fetch user by valid login") {
        val fetchedUser =
            userOperations
                .selectUserByLogin(insertedUser.login)
                .shouldNotBeNull()

        fetchedUser.login.shouldBe(validLogin)
        fetchedUser.name.shouldBe(validName)
        fetchedUser.surname.shouldBe(validSurname)
        fetchedUser.password
            .shouldBe(appConfiguredPasswordHasher.hash(validPass))
        fetchedUser.role.shouldBe(Role.STUDENT)
        fetchedUser.id.shouldBe(insertedUser.id)
    }

    test("User cant be fetched by invalid id") {
        userOperations
            .selectUserById(insertedUser.id + 2)
            .shouldBeNull()
    }

    test("User cant be fetched by invalid login") {
        userOperations
            .selectUserByLogin(insertedUser.login + "1")
            .shouldBeNull()
    }

    test("Select user name and surname should return entity with this fields") {
        userOperations
            .selectUserNameSurname(insertedUser.id)
            .shouldNotBeNull()
            .shouldBe(UserNameSurnameForAllResults(name = insertedUser.name, surname = insertedUser.surname))
    }

    test("Cant select user name and surname by invalid user id") {
        userOperations
            .selectUserNameSurname(insertedUser.id + 2)
            .shouldBeNull()
    }

    test("Select user data without pass should return entity with this fields") {
        val validUsers = listOf(insertedUser, insertedAdmin)
        userOperations
            .selectAllUsersWithoutPassword()
            .shouldNotBeNull()
            .shouldBe(validUsers.map { UserDataForUsersList(it.id.toString(), it.login, it.name, it.surname) })
    }
})
