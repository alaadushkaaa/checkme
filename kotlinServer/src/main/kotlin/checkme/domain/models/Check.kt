package checkme.domain.models

import checkme.config.CheckDatabaseConfig
import checkme.config.LoggingConfig
import checkme.domain.checks.CheckDataConsole
import checkme.domain.checks.CheckDataSQL
import checkme.domain.checks.Criterion
import checkme.domain.forms.CheckResult
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.time.LocalDateTime

@Suppress("LongParameterList")
data class Check(
    val id: Int,
    val taskId: Int,
    val userId: Int,
    val date: LocalDateTime,
    val result: Map<String, CheckResult>,
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
            loggingConfig: LoggingConfig,
        ): Map<String, CheckResult>? {
            val results = mutableMapOf<String, CheckResult>()
            beforeAllCriterionCheck(
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                results = results,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
            )
            for (criterion in task.criterions.filter { !specialCriteria.contains(it.value.test) }) {
                beforeEachCriterionCheck(
                    task = task,
                    checkId = checkId,
                    user = user,
                    answers = answers,
                    results = results,
                    checkDatabaseConfig = checkDatabaseConfig,
                    loggingConfig = loggingConfig
                )
                if (!specialCriteria.contains(criterion.value.test)) {
                    val checkResult = criterionCheck(
                        criterion = criterion,
                        task = task,
                        checkId = checkId,
                        user = user,
                        answers = answers,
                        checkDatabaseConfig = checkDatabaseConfig,
                        loggingConfig = loggingConfig
                    ) ?: return null
                    results[criterion.key] = checkResult
                }
                afterEachCriterionCheck(
                    task = task,
                    checkId = checkId,
                    user = user,
                    answers = answers,
                    results = results,
                    checkDatabaseConfig = checkDatabaseConfig,
                    loggingConfig = loggingConfig
                )
            }
            afterAllCriterionCheck(
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                results = results,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
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
            loggingConfig: LoggingConfig,
        ) {
            val specialResultBeforeAll = tryCheckSpecialCriterionAll(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "beforeAll.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
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
            loggingConfig: LoggingConfig,
        ) {
            val specialResultBeforeEach = results.tryCheckSpecialCriterionEach(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "beforeEach.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
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
            loggingConfig: LoggingConfig,
        ) {
            val specialResultAfterAll = tryCheckSpecialCriterionAll(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "afterAll.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
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
            loggingConfig: LoggingConfig,
        ) {
            val specialResultAfterEach = results.tryCheckSpecialCriterionEach(
                specialCriterion = task.criterions.entries.firstOrNull { it.value.test == "afterEach.json" },
                task = task,
                checkId = checkId,
                user = user,
                answers = answers,
                checkDatabaseConfig = checkDatabaseConfig,
                loggingConfig = loggingConfig
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
            loggingConfig: LoggingConfig,
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
                        criterionCheck(
                            criterion = specialCriterion,
                            task = task,
                            checkId = checkId,
                            user = user,
                            answers = answers,
                            checkDatabaseConfig = checkDatabaseConfig,
                            loggingConfig = loggingConfig
                        )
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
            loggingConfig: LoggingConfig,
        ): Pair<String, CheckResult>? {
            val allResult = specialCriterion
                ?.let {
                    criterionCheck(
                        criterion = it,
                        task = task,
                        checkId = checkId,
                        user = user,
                        answers = answers,
                        checkDatabaseConfig = checkDatabaseConfig,
                        loggingConfig = loggingConfig
                    )
                }
                ?: return null
            return Pair(specialCriterion.key, allResult)
        }

        @Suppress("UnusedParameter")
        private fun criterionCheck(
            criterion: Map.Entry<String, Criterion>,
            task: Task,
            checkId: Int,
            user: User,
            answers: List<Pair<String, String>>,
            checkDatabaseConfig: CheckDatabaseConfig,
            loggingConfig: LoggingConfig,
        ): CheckResult? {
            // todo answers могут понадобиться для следующих проверок
            val objectMapper = jacksonObjectMapper()
            val checkFile = findCheckFile("../tasks/${task.name}", criterion.value.test)
            val jsonString = checkFile?.readText()
            if (jsonString == null) {
                ServerLogger.log(
                    user = user,
                    action = "Check task warnings",
                    message = "Check failed, file for task ${task.id}-${task.name} criterion ${criterion.value.test} " +
                        "not found",
                    type = LoggerType.WARN
                )
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
                        val dbScript = jsonWithCheck.get("dbScript").asText().toString()
                        val scripts = dbScript.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        val check = CheckDataSQL(
                            type = CheckType.SQL_CHECK,
                            dbScript = scripts,
                            referenceQuery = jsonWithCheck.get("referenceQuery").asText().toString()
                        )
                        CheckDataSQL.sqlCheck(
                            task = task,
                            checkDataSQL = check,
                            user = user,
                            checkId = checkId,
                            criterion = criterion.value,
                            overall = loggingConfig.overall,
                            config = checkDatabaseConfig
                        )
                    }

                    else -> {
                        ServerLogger.log(
                            user = user,
                            action = "Add task warnings",
                            message = "Unknown check type ${task.id}-${task.name} criterion ${criterion.value.test}",
                            type = LoggerType.WARN
                        )
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
