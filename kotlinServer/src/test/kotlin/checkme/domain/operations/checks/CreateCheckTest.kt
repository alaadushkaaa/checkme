package checkme.domain.operations.checks

import checkme.db.validChecks
import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import java.time.LocalDateTime

class CreateCheckTest : FunSpec({
    val checks = mutableListOf<Check>()
    isolationMode = IsolationMode.InstancePerTest

    val insertCheckMock: (
        taskId: Int,
        userId: Int,
        date: LocalDateTime,
        result: Map<String, CheckResult>,
        status: String,
    ) -> Check? = {
            taskId,
            userId,
            date,
            result,
            status,
        ->
        val check =
            Check(
                id = checks.size + 1,
                taskId = taskId,
                userId = userId,
                date = date,
                result = result,
                status = status
            )

        checks.add(
            Check(
                id = checks.size + 1,
                taskId = taskId,
                userId = userId,
                date = date,
                result = result,
                status = status
            )
        )
        check
    }
    val insertCheckNullMock: (
        taskId: Int,
        userId: Int,
        date: LocalDateTime,
        result: Map<String, CheckResult>?,
        status: String,
    ) -> Check? = { _, _, _, _, _ -> null }

    val createCheck = CreateCheck(insertCheckMock)

    val createNullCheck = CreateCheck(
        insertCheckNullMock
    )

    test("Valid check can be inserted") {
        val validTask = validChecks.first()
        createCheck(
            taskId = validTask.taskId,
            userId = validTask.userId,
            date = validTask.date,
            result = validTask.result,
            status = validTask.status
        ).shouldBeSuccess()
    }

    test("Unknown db error test for insert check in bd") {
        val validTask = validChecks.first()
        createNullCheck(
            taskId = validTask.taskId,
            userId = validTask.userId,
            date = validTask.date,
            result = validTask.result,
            status = validTask.status
        ).shouldBeFailure(CreateCheckError.UNKNOWN_DATABASE_ERROR)
    }

    test("task with null result can be inserted") {
        val validTask = validChecks.first().copy(result = emptyMap())
        createCheck(
            taskId = validTask.taskId,
            userId = validTask.userId,
            date = validTask.date,
            result = validTask.result,
            status = validTask.status
        ).shouldBeSuccess()
    }
})
