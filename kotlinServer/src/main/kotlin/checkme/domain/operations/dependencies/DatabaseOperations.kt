package checkme.domain.operations.dependencies

import checkme.domain.operations.dependencies.bundles.BundlesDatabase
import checkme.domain.operations.dependencies.checks.ChecksDatabase
import checkme.domain.operations.dependencies.tasks.TasksDatabase
import checkme.domain.operations.dependencies.users.UsersDatabase

interface DatabaseOperations {
    val userOperations: UsersDatabase
    val checkOperations: ChecksDatabase
    val taskOperations: TasksDatabase
    val bundleOperations: BundlesDatabase
}
