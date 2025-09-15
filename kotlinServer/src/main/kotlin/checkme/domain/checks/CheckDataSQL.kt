package checkme.domain.checks

import checkme.config.CheckDatabaseConfig
import checkme.domain.forms.CheckResult
import checkme.domain.models.CheckType
import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.service.SqlCheckService
import checkme.web.solution.handlers.SOLUTIONS_DIR
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import java.io.File

const val DB_NAME = "SqlDb"

@Suppress("LongParameterList")
data class CheckDataSQL(
    val type: CheckType,
    val dbScript: String,
    val referenceQuery: String,
) {
    companion object {
        fun sqlCheck(
            task: Task,
            checkDataSQL: CheckDataSQL,
            answer: List<Pair<String, String>>,
            user: User,
            checkId: Int,
            criterion: Criterion,
            config: CheckDatabaseConfig,
        ): CheckResult {
            val answerSql = answer.first().second.substringAfter("value=").substringBefore(", headers")
            addUserAnswerSqlToDir(
                user = user,
                taskName = task.name,
                checkId = checkId,
                answer = answerSql
            )
            val sqlScript = findCScriptFile(
                scriptName = checkDataSQL.dbScript,
                taskName = task.name
            )
            if (sqlScript == null || !sqlScript.exists()) {
                return CheckResult(
                    0,
                    "Check failed: Setup SQL script ${checkDataSQL.dbScript} not found."
                )
            }

            val setupSql = sqlScript.readText()
            return tryGetCheckResults(
                task = task,
                checkDataSQL = checkDataSQL,
                studentQuery = answerSql,
                user = user,
                checkId = checkId,
                criterion = criterion,
                config = config,
                setupSql = setupSql
            )
        }

        private fun findCScriptFile(
            scriptName: String,
            taskName: String,
        ): File? {
            val dir = File("../tasks/$taskName")
            if (!dir.isDirectory) return null
            return dir.listFiles()?.firstOrNull { it.name == scriptName }
        }

        private fun tryGetCheckResults(
            task: Task,
            checkDataSQL: CheckDataSQL,
            studentQuery: String,
            user: User,
            checkId: Int,
            criterion: Criterion,
            config: CheckDatabaseConfig,
            setupSql: String,
        ): CheckResult {
            return when (
                val queriesResults = SqlCheckService(config).getSqlResults(
                    firstScript = setupSql,
                    referenceQuery = checkDataSQL.referenceQuery,
                    studentQuery = studentQuery,
                    checkId = checkId,
                    userId = user.id
                )
            ) {
                is Failure -> {
                    println(
                        "При выполнении теста ${criterion.test} задания " +
                            "${task.name}-${task.id} произошла ошибка: ${queriesResults.reason.trim()}"
                    )
                    CheckResult(0, "Something was wrong with check. Ask for help")
                }

                is Success -> {
                    val studentResult = queriesResults.value.first
                    val referenceResult = queriesResults.value.second
                    if (studentResult == referenceResult) {
                        CheckResult(criterion.score, criterion.description)
                    } else {
                        CheckResult(0, criterion.message)
                    }
                }
            }
        }

        private fun addUserAnswerSqlToDir(
            user: User,
            taskName: String,
            checkId: Int,
            answer: String,
        ) {
            val solutionDir = File(
                "..$SOLUTIONS_DIR" +
                    "/${user.name}-${user.surname}-${user.login}" +
                    "/$taskName" +
                    "/$checkId"
            )
            if (!solutionDir.exists()) {
                solutionDir.mkdirs()
            }
            val filePath = File(solutionDir, "$checkId.sql")
            filePath.writeBytes(answer.toByteArray())
        }
    }
}
