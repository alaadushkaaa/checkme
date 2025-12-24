package ru.yarsu.contentPages.content.taskListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.content.hiddenTask.TaskHiddenButton
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.task.TaskFormatForList
import kotlin.uuid.ExperimentalUuidApi

class TaskListViewer(
    private val serverUrl: String,
    private val routing: Routing,
    taskList: List<TaskFormatForList>
) : VPanel() {
    init {
        for (task in taskList){
            if (task.isActual || UserInformationStorage.isAdmin()) {
                hPanel(className = "task-in-list") {
                    val taskItem = VPanel(className = "task-item") {
                        div(task.name, className = "name")
                        val description = task.description
                            .replace("(<([^>]+)>)".toRegex(), "")
                        div(
                            if (description.length > 50)
                                "" + description.filterIndexed { index, _ -> index <= 50 } + "..."
                            else description
                        )
                    }.apply {
                        this.onClick {
                            @OptIn(ExperimentalUuidApi::class)
                            routing.navigate("task/${task.id}")
                        }
                    }
                    this.add(taskItem)
                    if (UserInformationStorage.isAdmin()) {
                        @OptIn(ExperimentalUuidApi::class)
                        val hiddenButton = TaskHiddenButton(
                            serverUrl,
                            task.isActual,
                            task.id,
                            this
                        )
                        this.add(hiddenButton)
                    }
                }
            }
        }
    }
}