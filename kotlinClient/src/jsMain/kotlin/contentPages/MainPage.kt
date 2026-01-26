package ru.yarsu.contentPages

import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.componentsPage.Content
import ru.yarsu.contentPages.componentsPage.Footer
import ru.yarsu.contentPages.componentsPage.Header
import ru.yarsu.contentPages.content.addBundlePage.AddBundle
import ru.yarsu.contentPages.content.addBundlePage.ChangeBundleTasksOrder
import ru.yarsu.contentPages.content.addBundlePage.SelectBundleTasks
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.contentPages.content.addTaskPage.AddTask
import ru.yarsu.contentPages.content.bundlesPages.Bundle
import ru.yarsu.contentPages.content.bundlesPages.BundlesList
import ru.yarsu.contentPages.content.bundlesPages.ChangeBundleName
import ru.yarsu.contentPages.content.journalPage.Journal
import ru.yarsu.contentPages.content.journalPage.LogFile
import ru.yarsu.contentPages.content.solutionsPages.AllSolutions
import ru.yarsu.contentPages.content.solutionPage.Solution
import ru.yarsu.contentPages.content.taskPage.Task
import ru.yarsu.contentPages.content.mySolutionListPage.MySolutionList
import ru.yarsu.contentPages.content.solutionsPages.AllSolutionsGroupByTask
import ru.yarsu.contentPages.content.solutionsPages.AllSolutionsTable
import ru.yarsu.contentPages.content.taskListPage.TaskList
import ru.yarsu.contentPages.content.solutionsPages.TaskOrUserSolutions
import ru.yarsu.contentPages.content.userListPage.UserList
import ru.yarsu.enumClasses.ListType
import kotlin.uuid.Uuid

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
            content.add(TaskList(serverUrl, routingMainPage, ListType.ALL))
        }).on("/bundle-list", {
            content.removeAll()
            content.add(BundlesList(serverUrl, routingMainPage, ListType.ALL))
        }).on("/hidden-bundle-list", {
            content.removeAll()
            content.add(BundlesList(serverUrl, routingMainPage, ListType.HIDDEN))
        }).on("/add-task", {
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                content.add(AddTask(serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/add-bundle", {
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                content.add(AddBundle(serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/bundle/change-name/:id", {match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val id = match.data.id.toString()
                content.add(ChangeBundleName(serverUrl, id, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/task/:id", { match ->
            content.removeAll()
            val id = try {
                Uuid.parse(match.data.id.toString())
            } catch (_: IllegalArgumentException) {
                null
            }
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
            val id = try {
                Uuid.parse(match.data.id.toString())
            } catch (_: IllegalArgumentException) {
                null
            }
            content.add(Solution(id, serverUrl, routingMainPage))
        }).on("/solution-list/:page", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val page = match.data.page.toString().toIntOrNull()
                content.add(AllSolutions(page, serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/task-solutions-list/:page", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val page = match.data.page.toString().toIntOrNull()
                content.add(AllSolutionsGroupByTask(page, serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/solution-list/user/:id", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val id = try {
                    Uuid.parse(match.data.id.toString())
                } catch (_: IllegalArgumentException) {
                    null
                }
                content.add(TaskOrUserSolutions(id, "user", serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/solution-list/task/:id", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val id = try {
                    Uuid.parse(match.data.id.toString())
                } catch (_: IllegalArgumentException) {
                    null
                }
                content.add(TaskOrUserSolutions(id, "task", serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/solutions-table", { match ->
            content.removeAll()
            content.add(AllSolutionsTable(serverUrl, routingMainPage))
        }).on("/hidden-task-list", {
            content.removeAll()
            content.add(TaskList(serverUrl, routingMainPage, ListType.HIDDEN))
        }).on("/bundle/select-bundle-tasks/:id", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val id = try {
                    Uuid.parse(match.data.id.toString())
                } catch (_: IllegalArgumentException) {
                    null
                }
                content.add(SelectBundleTasks(id.toString(), serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("bundle/select-order/:id", { match ->
            if (UserInformationStorage.isAdmin()) {
                content.removeAll()
                val id = try {
                    Uuid.parse(match.data.id.toString())
                } catch (_: IllegalArgumentException) {
                    null
                }
                content.add(ChangeBundleTasksOrder(id.toString(), serverUrl, routingMainPage))
            } else {
                routingMainPage.navigate("/")
            }
        }).on("/bundle/:id", { match ->
            content.removeAll()
            val id = try {
                Uuid.parse(match.data.id.toString())
            } catch (_: IllegalArgumentException) {
                null
            }
            content.add(Bundle(id, serverUrl, routingMainPage))
        }).on("/journal/file/:name", { match ->
            content.removeAll()
            val name = match.data.name.toString()
            content.add(LogFile(serverUrl, routingMainPage, name))
        }).on("/journal/:page", { match ->
            content.removeAll()
            val page = match.data.page.toString().toIntOrNull()
            content.add(Journal(page, serverUrl, routingMainPage))
        }).resolve()
    }
}