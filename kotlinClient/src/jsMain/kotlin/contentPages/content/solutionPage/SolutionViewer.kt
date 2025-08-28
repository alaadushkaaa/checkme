package ru.yarsu.contentPages.content.solutionPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.Loading
import ru.yarsu.serializableClasses.solution.SolutionFormat

class SolutionViewer(
    private val solution: SolutionFormat,
    private val routing: Routing
) : VPanel() {
    init {
        if ((solution.status == "В процессе") || (solution.result == null)) {
            add(Loading("Проверяем эту задачу"))
        } else if (solution.status == "Проверено") {
            val score = solution.result.values.map { it.score }.toList().sum()
            h2("Результат: $score")
            div(solution.task.name, className = "task-link").onClick {
                routing.navigate("task/${solution.task.id}")
            }
            hPanel(className="criteria-list") {
                div("Критерий", className="criteria-message title")
                div("Баллы", className="criteria-score title")
            }
            solution.result.values.forEach { (score, message) ->
                hPanel(className="criteria-list") {
                    div(message, className=if (score > 0) "criteria-message criteria-passed" else "criteria-message criteria-failed")
                    div(score.toString(), className="criteria-score")
                }
            }
        }
    }
}