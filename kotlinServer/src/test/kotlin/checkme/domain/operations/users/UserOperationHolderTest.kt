package checkme.domain.operations.users

import checkme.db.config
import checkme.domain.operations.dependencies.users.UsersDatabase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class UserOperationHolderTest : FunSpec({
    val usersOperations: UsersDatabase = mock()
    val userOperationsHolder = UserOperationHolder(usersOperations, config)

    test("UserOperationsHolder should initialize with provided user operations") {
        userOperationsHolder.createUser::class.shouldBe(CreateUser::class)
        userOperationsHolder.fetchUserById::class.shouldBe(FetchUserById::class)
        userOperationsHolder.fetchUsersByRole::class.shouldBe(FetchUsersByRole::class)
        userOperationsHolder.fetchUserByLogin::class.shouldBe(FetchUserByLogin::class)
        userOperationsHolder.fetchUserNameSurname::class.shouldBe(FetchUserNameSurname::class)
        userOperationsHolder.fetchUsersDataWithoutPassword::class.shouldBe(FetchUsersDataWithoutPassword::class)
    }
})
