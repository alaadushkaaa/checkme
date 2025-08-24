package ru.yarsu.contentPages.content.userListPage

import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.VPanel
import ru.yarsu.serializableClasses.user.UserInList

class UserListViewer(
    userList: List<UserInList>
) : VPanel(className = "UserList") {
    init {
        for (user in userList) {
            div("${user.surname} ${user.name} (${user.login})")
            button("Изменить роль", className = "usually-button") // Кнопка "Изменить роль" на будущее
        }
    }
}