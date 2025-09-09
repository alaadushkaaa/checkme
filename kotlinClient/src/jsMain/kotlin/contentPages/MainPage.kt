package ru.yarsu.contentPages

import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.componentsPage.Content
import ru.yarsu.contentPages.componentsPage.Footer
import ru.yarsu.contentPages.componentsPage.Header
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.contentPages.content.addTaskPage.AddTask
import ru.yarsu.contentPages.content.solutionsPages.AllSolutions
import ru.yarsu.contentPages.content.solutionPage.Solution
import ru.yarsu.contentPages.content.taskPage.Task
import ru.yarsu.contentPages.content.mySolutionListPage.MySolutionList
import ru.yarsu.contentPages.content.taskListPage.TaskList
import ru.yarsu.contentPages.content.solutionsPages.TaskOrUserSolutions
import ru.yarsu.contentPages.content.userListPage.UserList

class MainPage(
    private val serverUrl: String,
    routing: Routing,
) : SimplePanel(className = "app-content") {
    private val routingMainPage = Routing.init("/")
    init {
        val userName = UserInformationStorage.getUserInformation()?.username
        if (userName == null){
            routing.navigate("/authorization/sign_in")
        }
        val header = Header(routing, routingMainPage)
        val content = Content(serverUrl, routingMainPage)
        val footer = Footer()
        div(className = "app-content"){
            add(header)
        }
        div(className = "app-content main-page"){
            add(content)
        }
        div(className = "app-content footer"){
            add(footer)
        }
        routingMainPage.on("/", {
            content.removeAll()
            content.add(TaskList(serverUrl, routingMainPage))
        }).on("/add-task", {
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                content.add(AddTask(serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/task/:id", { match ->
            content.removeAll()
            val id = match.data.id.toString().toIntOrNull()
            content.add(Task(id, serverUrl, routingMainPage))
        }).on("/user-list",{
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                content.add(UserList(serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/my-solution-list", {
            content.removeAll()
            content.add(MySolutionList(serverUrl, routingMainPage))
        }).on("/solution/:id", { match ->
            content.removeAll()
            val id = match.data.id.toString().toIntOrNull()
            content.add(Solution(id, serverUrl, routingMainPage))
        }).on("/solution-list/:page", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val page = match.data.page.toString().toIntOrNull()
                content.add(AllSolutions(page, serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/solution-list/user/:id", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val id = match.data.id.toString().toIntOrNull()
                content.add(TaskOrUserSolutions(id, "user", serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/solution-list/task/:id", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val id = match.data.id.toString().toIntOrNull()
                content.add(TaskOrUserSolutions(id, "task", serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).resolve()
    }
}