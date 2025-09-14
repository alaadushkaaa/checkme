package checkme.domain.models

import checkme.config.CheckDatabaseConfig
import checkme.domain.checks.CheckDataConsole
import checkme.domain.checks.CheckDataSQL
import checkme.domain.checks.Criterion
import checkme.domain.forms.CheckResult
import checkme.domain.models.Check.Companion.tryCheckSpecialCriterionEach
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.*
import java.io.File
import java.time.LocalDateTime

@Suppress("LongParameterList")
data class Check(
    val id: Int,
    val taskId: Int,
    val userId: Int,
    val date: LocalDateTime,
    val result: Map<String, CheckResult>?,
    val status: String,
) {
    companion object {
        private val specialCriteria = listOf("beforeAll.json", "beforeEach.json", "afterEach.json", "afterAll.json")

        internal fun checkStudentAnswer(
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
        ): Map<String, CheckResult>? {
            val results = mutableMapOf<String, CheckResult>()
            beforeAllCriterionCheck(
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                results = results,
                checkDatabaseConfig = checkDatabaseConfig
            )
            for (criterion in task.criterions.filter { !specialCriteria.contains(it.value.test) }) {
                beforeEachCriterionCheck(
                    task = task,
                    checkId = checkId,
                    user = user,
                    answers = answers,
                    results = results,
                    checkDatabaseConfig = checkDatabaseConfig
                )
                if (!specialCriteria.contains(criterion.value.test)) {
                    val checkResult = criterionCheck(
                        criterion = criterion,
                        task = task,
                        checkId = checkId,
                        user = user,
                        answers = answers,
                        checkDatabaseConfig = checkDatabaseConfig
                    ) ?: return null
                    results[criterion.key] = checkResult
                }
                afterEachCriterionCheck(
                    task = task,
                    checkId = checkId,
                    user = user,
                    answers = answers,
                    results = results,
                    checkDatabaseConfig = checkDatabaseConfig
                )
            }
            afterAllCriterionCheck(
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                results = results,
                checkDatabaseConfig = checkDatabaseConfig
            )
            return results
        }

        private fun beforeAllCriterionCheck(
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
            checkDatabaseConfig: CheckDatabaseConfig,
        ) {
            val specialResultBeforeAll = tryCheckSpecialCriterionAll(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "beforeAll.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig
            )
            if (specialResultBeforeAll != null) {
                results[specialResultBeforeAll.first] = specialResultBeforeAll.second
            }
        }

        private fun beforeEachCriterionCheck(
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
            checkDatabaseConfig: CheckDatabaseConfig,
        ) {
            val specialResultBeforeEach = results.tryCheckSpecialCriterionEach(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "beforeEach.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig
            )

            if (specialResultBeforeEach != null) {
                results[specialResultBeforeEach.first] = specialResultBeforeEach.second
            }
        }

        private fun afterAllCriterionCheck(
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
            checkDatabaseConfig: CheckDatabaseConfig,
        ) {
            val specialResultAfterAll = tryCheckSpecialCriterionAll(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "afterAll.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig
            )
            if (specialResultAfterAll != null) {
                results[specialResultAfterAll.first] = specialResultAfterAll.second
            }
        }

        private fun afterEachCriterionCheck(
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
            checkDatabaseConfig: CheckDatabaseConfig,
        ) {
            val specialResultAfterEach = results.tryCheckSpecialCriterionEach(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "afterEach.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig
            )

            if (specialResultAfterEach != null) {
                results[specialResultAfterEach.first] = specialResultAfterEach.second
            }
        }

        private fun MutableMap<String, CheckResult>.tryCheckSpecialCriterionEach(
            specialCriterion: Map.Entry<String, Criterion>?,
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
        ): Pair<String, CheckResult>? {
            return if (
                (
                    (this.criterionAlreadyChecked(specialCriterion)) ||
                        (this[specialCriterion?.key] == null)
                ) &&
                specialCriterion != null
            ) {
                when (
                    val eachResult =
                        criterionCheck(specialCriterion, task, checkId, user, answers, checkDatabaseConfig)
                ) {
                    is CheckResult -> Pair(specialCriterion.key, eachResult)
                    else -> null
                }
            } else {
                null
            }
        }

        private fun tryCheckSpecialCriterionAll(
            specialCriterion: Map.Entry<String, Criterion>?,
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
        ): Pair<String, CheckResult>? {
            val allResult = specialCriterion
                ?.let { criterionCheck(it, task, checkId, user, answers, checkDatabaseConfig) }
                ?: return null
            return Pair(specialCriterion.key, allResult)
        }

        private fun criterionCheck(
            criterion: Map.Entry<String, Criterion>,
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
        ): CheckResult? {
            val objectMapper = jacksonObjectMapper()
            val checkFile = findCheckFile("../tasks/${task.name}", criterion.value.test)
            val jsonString = checkFile?.readText()
            if (jsonString == null) {
                return null
            } else {
                val jsonWithCheck = objectMapper.readTree(jsonString)
                val type = jsonWithCheck.get("type")?.asText()
                return when (type.toString()) {
                    CheckType.CONSOLE_CHECK.code -> {
                        val check = CheckDataConsole(
                            type = CheckType.CONSOLE_CHECK,
                            command = jsonWithCheck.get("command").asText().toString(),
                            expected = jsonWithCheck.get("expected").asText().toString()
                        )
                        CheckDataConsole.consoleCheck(task, check, user, checkId, criterion.value)
                    }

                    CheckType.SQL_CHECK.code -> {
                        val check = CheckDataSQL(
                            type = CheckType.SQL_CHECK,
                            dbScript = jsonWithCheck.get("dbScript").asText().toString(),
                            referenceQuery = jsonWithCheck.get("referenceQuery").asText().toString()
                        )
                        CheckDataSQL.sqlCheck(task, check, answers, user, checkId, criterion.value, checkDatabaseConfig)
                    }

                    else -> {
                        println("Неизвестный тип проверки")
                        null
                    }
                }
            }
        }

        private fun findCheckFile(
            directoryPath: String,
            fileName: String,
        ): File? {
            val dir = File(directoryPath)
            if (!dir.isDirectory) return null
            return dir.listFiles()?.firstOrNull { it.name == fileName }
        }
    }
}

private fun MutableMap<String, CheckResult>.criterionAlreadyChecked(specialCriterion: Map.Entry<String, Criterion>?) =
    this[specialCriterion?.key] != null && this[specialCriterion?.key]?.score != 0

enum class CheckType(val code: String) {
    CONSOLE_CHECK("console-check"),
    SQL_CHECK("sql-check"),
}
