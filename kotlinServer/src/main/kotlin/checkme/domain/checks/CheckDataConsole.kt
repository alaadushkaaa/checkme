package checkme.domain.checks

import checkme.domain.forms.CheckResult
import checkme.domain.models.CheckType
import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.solution.handlers.SOLUTIONS_DIR
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.io.File
import java.util.concurrent.TimeUnit

// Извлекается команда из CheckDataConsole, после чего она запускается в нужной директории с решением пользователя
// Результат команды сравнивается с ожидаемым expected CheckDataConsole, после чего присваиваются баллы.
// В случае некорректного теста студенту по данному критерию присваивается 0 баллов, а в сообщение записывается ошибка

const val MINUTE_TIMEOUT = 60

data class CheckDataConsole(
    val type: CheckType,
    val command: String,
    val expected: String,
) {
    companion object {
        fun consoleCheck(
            task: Task,
            checkDataConsole: CheckDataConsole,
            user: User,
            checkId: Int,
            criterion: Criterion,
        ): CheckResult {
            val command = checkDataConsole.command
            val directoryPath = "..$SOLUTIONS_DIR" +
                    "/${user.name}-${user.surname}-${user.login}" +
                    "/${task.name}" +
                    "/$checkId"
            if (!File(directoryPath).exists()) {
                ServerLogger.log(
                    user = user,
                    action = "Check task warnings",
                    message = "Check failed, file for solution (check $checkId) not found",
                    type = LoggerType.WARN
                )
                return CheckResult(0, "Check failed, file for solution check $checkId not found")
            }
            return when (
                val output = runCommandInDirectory(
                    "..$SOLUTIONS_DIR" +
                            "/${user.name}-${user.surname}-${user.login}" +
                            "/${task.name}" +
                            "/$checkId",
                    command
                )
            ) {
                is Success -> {
                    if (output.value.trim() != checkDataConsole.expected.trim()) {
                        CheckResult(0, criterion.message)
                    } else {
                        CheckResult(criterion.score, criterion.description)
                    }
                }

                is Failure -> {
                    ServerLogger.log(
                        user = user,
                        action = "Check task warnings",
                        message = "An error occurred while running check ${criterion.test} for task \" +\n" +
                                "\"${task.name}-${task.id}: ${output.reason.trim()}",
                        type = LoggerType.WARN
                    )
                    CheckResult(
                        0,
                        criterion.message
                    )
                }
            }
        }

        private fun runCommandInDirectory(
            directory: String,
            command: String,
        ): Result4k<String, String> {
            val process = ProcessBuilder("bash", "-c", command)
                .directory(File(directory))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            if (!process.waitFor(MINUTE_TIMEOUT.toLong(), TimeUnit.SECONDS)) {
                process.destroy()
                return Failure("Error: The time for the process has expired")
            }
            return if (error.isBlank()) Success(output) else Failure("Error: $error")
        }
    }
}
