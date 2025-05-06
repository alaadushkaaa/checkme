package checkme.db.checks

import checkme.db.generated.tables.references.CHECKS
import checkme.db.utils.safeLet
import checkme.domain.models.Check
import checkme.domain.operations.dependencies.ChecksDatabase
import checkme.web.solution.forms.CheckResult
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.JSONB.jsonb
import org.jooq.Record
import java.time.LocalDateTime

class CheckOperations (
    private val jooqContext: DSLContext,
) : ChecksDatabase {
    private val objectMapper = jacksonObjectMapper()

    override fun selectAllChecks(): List<Check> =
        selectFromChecks()
            .fetch()
            .mapNotNull { record: Record ->
                record.toCheck()
            }

    override fun selectCheckById(checkId: Int): Check? =
        selectFromChecks()
            .where(CHECKS.ID.eq(checkId))
            .fetchOne()
            ?.toCheck()

    override fun selectChecksByUserId(userId: Int): List<Check> =
        selectFromChecks()
            .where(CHECKS.USERID.eq(userId))
            .fetch()
            .mapNotNull { record: Record ->
                record.toCheck()
            }

    override fun updateCheckStatus(
        checkId: Int,
        status: String,
    ): Check? {
        return jooqContext.update(CHECKS)
            .set(CHECKS.STATUS, status)
            .where(CHECKS.ID.eq(checkId))
            .returningResult()
            .fetchOne()
            ?.toCheck()
    }

    override fun updateCheckResult(
        checkId: Int,
        result: Map<String, CheckResult>?,
    ): Check? {
        return jooqContext.update(CHECKS)
            .set(CHECKS.RESULT, jsonb(objectMapper.writeValueAsString(result)))
            .where(CHECKS.ID.eq(checkId))
            .returningResult()
            .fetchOne()
            ?.toCheck()
    }

    override fun insertCheck(
        taskId: Int,
        userId: Int,
        date: LocalDateTime,
        result: Map<String, CheckResult>?,
        status: String,
    ): Check? {
        return jooqContext.insertInto(CHECKS)
            .set(CHECKS.TASKID, taskId)
            .set(CHECKS.USERID, userId)
            .set(CHECKS.DATE, date)
            .set(CHECKS.RESULT, jsonb(objectMapper.writeValueAsString(result)))
            .set(CHECKS.STATUS, status)
            .returningResult()
            .fetchOne()
            ?.toCheck()
    }

    private fun selectFromChecks() =
        jooqContext
            .select(
                CHECKS.ID,
                CHECKS.TASKID,
                CHECKS.USERID,
                CHECKS.DATE,
                CHECKS.RESULT,
                CHECKS.STATUS
            )
            .from(CHECKS)
}

internal fun Record.toCheck(): Check? =
    safeLet(
        this[CHECKS.ID],
        this[CHECKS.TASKID],
        this[CHECKS.USERID],
        this[CHECKS.DATE],
        this[CHECKS.RESULT],
        this[CHECKS.STATUS],
    ) {
            id,
            taskId,
            userId,
            date,
            result,
            status,
        ->
        Check(
            id = id,
            taskId = taskId,
            userId = userId,
            date = date,
            result = jacksonObjectMapper().readValue<Map<String, CheckResult>>(result.data()),
            status
        )
    }
