package checkme.domain.models

import checkme.domain.checks.CheckDataConsole
import checkme.domain.checks.Criterion
import checkme.domain.forms.CheckResult
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.time.LocalDateTime

data class Check(
    val id: Int,
    val taskId: Int,
    val userId: Int,
    val date: LocalDateTime,
    val result: Map<String, CheckResult>?,
    val status: String,
) {
    companion object {
        private val specialCriteria = listOf("beforeAll.json", " beforeEach.json", "afterEach.json", "afterAll.json")

        internal fun checkStudentAnswer(
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
        ): Map<String, CheckResult>? {
            val results = mutableMapOf<String, CheckResult>()
            beforeAllCriterionCheck(
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                results = results
            )
            for (criterion in task.criterions) {
                beforeEachCriterionCheck(
                    task = task,
                    checkId = checkId,
                    user = user,
                    answers = answers,
                    results = results
                )
                if (!specialCriteria.contains(criterion.value.test)) {
                    val checkResult = criterionCheck(criterion, task, checkId, user, answers) ?: return null
                    results[criterion.key] = checkResult
                }
                afterEachCriterionCheck(
                    task = task,
                    checkId = checkId,
                    user = user,
                    answers = answers,
                    results = results
                )
            }
            afterAllCriterionCheck(task, checkId, user, answers, results)
            return results
        }

        private fun beforeAllCriterionCheck(
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            results: MutableMap<String, CheckResult>,
        ) {
            val specialResultBeforeAll = tryCheckSpecialCriterionAll(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "beforeAll.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers
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
        ) {
            val specialResultBeforeEach = results.tryCheckSpecialCriterionEach(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "beforeEach.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers
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
        ) {
            val specialResultAfterAll = tryCheckSpecialCriterionAll(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "afterAll.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers
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
        ) {
            val specialResultBeforeEach = results.tryCheckSpecialCriterionEach(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "beforeEach.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers
            )

            if (specialResultBeforeEach != null) {
                results[specialResultBeforeEach.first] = specialResultBeforeEach.second
            }
        }

        private fun MutableMap<String, CheckResult>.tryCheckSpecialCriterionEach(
            specialCriterion: Map.Entry<String, Criterion>?,
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
        ): Pair<String, CheckResult>? {
            return if (this[specialCriterion?.key] != null &&
                this[specialCriterion?.key]?.score != 0 &&
                specialCriterion != null
            ) {
                when (val eachResult = criterionCheck(specialCriterion, task, checkId, user, answers)) {
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
        ): Pair<String, CheckResult>? {
            val allResult = specialCriterion
                ?.let { criterionCheck(it, task, checkId, user, answers) }
                ?: return null
            return Pair(specialCriterion.key, allResult)
        }

        @Suppress("UnusedParameter")
        // todo answers будут нужны, когда будут реализованы другие типы проверок
        private fun criterionCheck(
            criterion: Map.Entry<String, Criterion>,
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
        ): CheckResult? {
            val objectMapper = jacksonObjectMapper()
            val checkFile = findCheckFile("../tasks/${task.id}-${task.name}", criterion.value.test)
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
                    else -> null
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

enum class CheckType(val code: String) {
    CONSOLE_CHECK("console-check"),
}
