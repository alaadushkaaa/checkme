package ru.yarsu.contentPages.content.allSolutionsPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import kotlinx.datetime.LocalDateTime
import ru.yarsu.serializableClasses.solution.SolutionFormatForAdminAllSolutionList

class AllSolutionsViewer(
    solutionListForAdmin: List<SolutionFormatForAdminAllSolutionList>,
    private val routing: Routing
) : VPanel() {
    init {
        for (solution in solutionListForAdmin){
            vPanel(className = "block") {
                div("Пользователь: ${solution.user.surname} ${solution.user.name}")
                div("Задача: ${solution.task.name}")
                div("Статус: ${solution.status}")
                if ((solution.status == "Проверено")) {
                    val score = solution.result?.values?.map { it.score }?.toList()?.sum()
                    div("Результат: $score")
                }
                val dateTime = LocalDateTime.parse(solution.date)
                div("${solution.status} - ${dateTime.date} ${dateTime.time}")
            }.onClick {
                routing.navigate("solution/${solution.id}")
            }
        }
    }
}