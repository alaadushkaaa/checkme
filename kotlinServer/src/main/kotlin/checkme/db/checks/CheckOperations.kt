package checkme.db.checks

import checkme.db.generated.tables.references.CHECKS
import checkme.db.generated.tables.references.TASKS
import checkme.db.generated.tables.references.USERS
import checkme.db.utils.safeLet
import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import checkme.domain.operations.dependencies.ChecksDatabase
import checkme.web.solution.forms.CheckWithAllData
import checkme.web.solution.forms.CheckWithTaskData
import checkme.web.solution.forms.TaskDataForAllResults
import checkme.web.solution.forms.UserDataForAllResults
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.JSONB.jsonb
import org.jooq.Record
import java.time.LocalDateTime

const val CHECKS_LIMIT = 10

class CheckOperations(
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
            .join(TASKS).on(CHECKS.TASKID.eq(TASKS.ID))
            .join(USERS).on(CHECKS.USERID.eq(USERS.ID))
            .where(CHECKS.USERID.eq(userId))
            .orderBy(CHECKS.ID)
            .fetch()
            .mapNotNull { record: Record ->
                record.toCheck()
            }

    override fun selectCheckByIdWithData(checkId: Int): CheckWithAllData? =
        selectFromChecks()
            .join(TASKS).on(CHECKS.TASKID.eq(TASKS.ID))
            .join(USERS).on(CHECKS.USERID.eq(USERS.ID))
            .where(CHECKS.ID.eq(checkId))
            .fetchOne()
            ?.let { record: Record -> record.toCheckWithAllData() }

    override fun selectAllChecksWithData(page: Int?): List<CheckWithAllData> {
        val query = selectFromChecks()
            .join(TASKS).on(CHECKS.TASKID.eq(TASKS.ID))
            .join(USERS).on(CHECKS.USERID.eq(USERS.ID))
            .orderBy(CHECKS.ID)
        page?.let {
            query
                .limit(CHECKS_LIMIT)
                .offset((page - 1) * CHECKS_LIMIT)
        }
        return query.fetch()
            .mapNotNull { record: Record -> record.toCheckWithAllData() }
    }

    override fun selectAllUsersChecks(userId: Int): List<CheckWithTaskData> =
        selectFromChecks()
            .join(TASKS).on(CHECKS.TASKID.eq(TASKS.ID))
            .where(CHECKS.USERID.eq(userId))
            .orderBy(CHECKS.ID)
            .fetch()
            .mapNotNull { record: Record -> record.toCheckWithTaskData() }

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

internal fun Record.toCheckWithAllData(): CheckWithAllData? =
    safeLet(
        this[CHECKS.ID],
        this[CHECKS.DATE],
        this[CHECKS.STATUS],
        this[USERS.NAME],
        this[USERS.SURNAME],
        this[TASKS.NAME],
    ) {
            id,
            date,
            status,
            userName,
            surname,
            taskName,
        ->
        CheckWithAllData(
            id = id.toString(),
            date = date,
            status = status,
            userData = UserDataForAllResults(
                name = userName,
                surname = surname
            ),
            taskData = TaskDataForAllResults(name = taskName),
        )
    }

internal fun Record.toCheckWithTaskData(): CheckWithTaskData? =
    safeLet(
        this[CHECKS.ID],
        this[CHECKS.DATE],
        this[CHECKS.STATUS],
        this[TASKS.NAME],
    ) {
            id,
            date,
            status,
            taskName,
        ->
        CheckWithTaskData(
            id = id.toString(),
            date = date,
            status = status,
            taskData = TaskDataForAllResults(name = taskName),
        )
    }
