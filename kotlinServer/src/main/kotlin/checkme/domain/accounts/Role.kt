package checkme.domain.accounts

enum class Role(
    val manageUsers: Boolean = false,
    val editTasks: Boolean = false,
    val setUserGroup: Boolean = false,
    val solveTasks: Boolean = false,
) {
    STUDENT(solveTasks = true),
    ADMIN(
        manageUsers = true,
        editTasks = true,
        setUserGroup = true
    ),
}
