package ru.yarsu.contentPages.content.taskListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.routing.Routing
import ru.yarsu.serializableClasses.task.TaskIdName

class TaskListViewer(
    private val routing: Routing,
    taskList: List<TaskIdName>
) : VPanel() {
    init {
        for (task in taskList){
            div(task.name, className = "block task-item").onClick {
                routing.navigate("task/${task.id}")
            }
        }
    }
}