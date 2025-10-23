package checkme.domain.operations.users

import checkme.db.validName
import checkme.db.validPassword
import checkme.db.validRole
import checkme.db.validSurname
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.web.solution.forms.UserDataForUsersList
import checkme.web.solution.forms.UserNameSurnameForAllResults
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class FetchUserTest : FunSpec({

    val users = listOf(
        User(
            id = 1,
            login = "user1",
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole,
        ),
        User(
            id = 2,
            login = "user2",
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = validRole,
        ),
        User(
            id = -1,
            login = "admin",
            name = validName,
            surname = validSurname,
            password = validPassword,
            role = Role.ADMIN,
        )
    )

    val fetchUserByIdMock: (Int) -> User? = { userId ->
        users.firstOrNull { it.id == userId }
    }
    val fetchUserByLoginMock: (String) -> User? = { login ->
        users.firstOrNull { it.login == login }
    }
    val fetchUsersByRoleMock: (Role) -> List<User>? = { userRole ->
        users.filter { it.role == userRole }
    }
    val fetchNoOneUsersByRoleMock: (Role) -> List<User>? = { userRole ->
        listOf(users.first()).filter { it.role == userRole }
    }
    val fetchUserNameSurnameMock: (Int) -> UserNameSurnameForAllResults? = { userId ->
        val user = users.firstOrNull { it.id == userId }
        user?.let {
            UserNameSurnameForAllResults(
                name = it.name,
                surname = user.surname
            )
        }
    }
    val fetchUsersDataWithoutPasswordMock: () -> List<UserDataForUsersList>? = {
        users.map {
            UserDataForUsersList(
                id = it.id.toString(),
                login = it.login,
                name = it.name,
                surname = it.surname
            )
        }
    }
    val fetchAllUsersMock: () -> List<User>? = { users }

    val fetchUserById = FetchUserById(fetchUserByIdMock)
    val fetchUserByLogin = FetchUserByLogin(fetchUserByLoginMock)
    val fetchUsersByRole = FetchUsersByRole(fetchUsersByRoleMock)
    val fetchUsersByRoleButNoUserWithThisRole = FetchUsersByRole(fetchNoOneUsersByRoleMock)
    val fetchUserNameSurname = FetchUserNameSurname(fetchUserNameSurnameMock)
    val fetchUsersDataWithoutPassword = FetchUsersDataWithoutPassword(fetchUsersDataWithoutPasswordMock)
    val fetchAllUsers = FetchAllUsers(fetchAllUsersMock)

    test("User can be fetched by valid id") {
        fetchUserById(users.first().id).shouldBeSuccess() shouldBe users.first()
    }

    listOf(
        users.minOf { it.id } - 1,
        users.maxOf { it.id } + 1,
    ).forEach { userID ->
        test("User can't be fetched by invalid id == $userID") {
            fetchUserById(userID)
                .shouldBeFailure(UserFetchingError.NO_SUCH_USER)
        }
    }

    test("User can be fetched by valid login") {
        fetchUserByLogin(users.first().login).shouldBeSuccess() shouldBe users.first()
    }

    listOf(
        "",
        "    ",
        "user3"
    ).forEach { login ->
        test("User can't be fetched by invalid login == $login") {
            fetchUserByLogin(login)
                .shouldBeFailure(UserFetchingError.NO_SUCH_USER)
        }
    }

    Role.entries.forEach { role ->
        test("User can be fetched by valid role -  $role") {
            fetchUsersByRole(role).shouldBeSuccess() shouldContainExactlyInAnyOrder users.filter { it.role == role }
        }
    }

    test("Fetch user by role returns empty list if no user with role") {
        fetchUsersByRoleButNoUserWithThisRole(Role.ADMIN).shouldBeSuccess() shouldBe emptyList()
    }

    test("Fetch user name and surname should return success if user exists") {
        fetchUserNameSurname(users.first().id).shouldBeSuccess() shouldBe
            UserNameSurnameForAllResults(users.first().name, users.first().surname)
    }

    listOf(
        users.minOf { it.id } - 1,
        users.maxOf { it.id } + 1,
    ).forEach { userId ->
        test("User name and surname can't be fetched by invalid id == $userId") {
            fetchUserNameSurname(userId)
                .shouldBeFailure(UserFetchingError.NO_SUCH_USER)
        }
    }

    test("Can fetch users data without password") {
        fetchUsersDataWithoutPassword().shouldBeSuccess() shouldContainExactlyInAnyOrder users.map {
            UserDataForUsersList(
                id = it.id.toString(),
                login = it.login,
                name = it.name,
                surname = it.surname
            )
        }
    }

    test("FetchAllUsers should return list of users") {
        fetchAllUsers().shouldBeSuccess().shouldHaveSize(users.size) shouldContainExactlyInAnyOrder users
    }
})
