package ru.yarsu.contentPages.content.userListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.routing.Routing
import ru.yarsu.serializableClasses.user.UserInList

class UserListViewer(
    userList: List<UserInList>,
    private val routing: Routing
) : VPanel(className = "UserList") {
    init {
        for (user in userList) {
            div("${user.surname} ${user.name} (${user.login})")
            div("Решения пользователя", className = "task-link").onClick {
                routing.navigate("solution-list/user/${user.id}")
            }
        }
    }
}