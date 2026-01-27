package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.core.onClick
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.panel.SimplePanel
import io.kvision.panel.VPanel
import io.kvision.routing.Routing
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.TableType
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import kotlinx.serialization.json.Json
import ru.yarsu.serializableClasses.solution.IdScore
import ru.yarsu.serializableClasses.solution.ResultScoreMessage
import ru.yarsu.serializableClasses.solution.SolutionInformation
import ru.yarsu.serializableClasses.solution.SolutionsTable

class AllSolutionsTableViewer(
    private val routing: Routing,
    private val solutionsTable: SolutionsTable,
): SimplePanel() {
    private fun getListMaxScore(solutions: List<SolutionInformation>) : List<IdScore> {
        return solutionsTable.tasks.map { task ->
            val max = solutions.filter { it.taskId == task.id }.maxOfOrNull { solution ->
                solution.result?.values?.map { it.score }?.toList()?.sum() ?: 0
            } ?: 0
            IdScore(task.id, max)
        }
    }

    private fun getData() : List<Map<String, String>> {
        return solutionsTable.solutions.toList().mapIndexed { index, user ->
            val userStats = solutionsTable.users.find { it.id == user.first }
            val surnameNameLogin = if (userStats != null) {
                "${userStats.surname} ${userStats.name} (${userStats.login})"
            } else {
                "Неизвестный пользователь"
            }
            val row = mutableMapOf<String, String>()
            row.put("id", user.first.toString())
            row.put("solutions", Json.Default.encodeToString(user.second))
            val listMaxScore = getListMaxScore(user.second)
            for (max in listMaxScore) {
                row.put("taskId${max.id}", max.score.toString())
            }
            row.put("surnameNameLogin", surnameNameLogin)
            row
        }
    }

    private fun getColorScore(score: Int?, result: Map<String, ResultScoreMessage>?): String {
        return if ((score == null) || (score == 0) || (result == null)) {
            "table-criteria-failed"
        } else {
            val criteriaScore = result.map { Pair(it.key, it.value.score) }.toMap()
            if (criteriaScore.values.contains(0)) {
                "table-criteria-fifty-fifty"
            } else {
                "table-criteria-passed"
            }
        }
    }

    private val columns = listOf<ColumnDefinition<Map<String, String>>>(
        ColumnDefinition(
            "Фамилия имя (логин)",
            field = "surnameNameLogin",
            headerFilter = Editor.INPUT
        )
    ) + solutionsTable.tasks.mapIndexed { index, title ->
        val taskId = title.id
        ColumnDefinition(
            title.name,
            field = "taskId$taskId",
            headerSort = false,
            formatterComponentFunction = { _, _, data ->
                val solutions = Json.Default.decodeFromString<List<SolutionInformation>>(
                    data.getValue("solutions")
                ).filter { it.taskId == taskId }
                VPanel().apply {
                    this.addAll(
                        solutions.map { solution ->
                            val score = solution.totalScore
                            Div("${solution.status}: $score", className = getColorScore(score, solution.result)).apply {
                                this.onClick {
                                    routing.navigate("/solution/${solution.id}")
                                }
                            }
                        }
                    )
                }
            }
        )
    }

    init {
        val table = tabulator(data = getData(), false,
            options = TabulatorOptions(
                columns = columns
            ),
            types = setOf(TableType.BORDERED)
        )
        button("Скачать", className = "usually-button").onClick {
            table.downloadCSV("solutionsTable")
        }
    }
}