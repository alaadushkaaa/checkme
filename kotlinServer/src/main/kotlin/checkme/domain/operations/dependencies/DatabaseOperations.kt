package checkme.domain.operations.dependencies

interface DatabaseOperations {
    val userOperations: UsersDatabase
    val checkOperations: ChecksDatabase
    val taskOperations: TasksDatabase
}
