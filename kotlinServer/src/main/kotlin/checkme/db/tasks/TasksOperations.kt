package checkme.db.tasks

import checkme.db.generated.tables.references.CHECKS
import checkme.db.generated.tables.references.TASKS
import checkme.db.utils.safeLet
import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.domain.operations.dependencies.tasks.TasksDatabase
import checkme.web.solution.forms.TaskNameForAllResults
import checkme.web.tasks.forms.TasksListData
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.JSONB.jsonb
import org.jooq.Record
import org.jooq.impl.DSL.commit
import org.jooq.impl.DSL.rollback

class TasksOperations(
    private val jooqContext: DSLContext,
) : TasksDatabase {
    private val objectMapper = jacksonObjectMapper()

    override fun selectTaskById(taskId: Int): Task? =
        selectFromTasks()
            .where(TASKS.ID.eq(taskId))
            .fetchOne()
            ?.toTask()

    override fun selectAllTask(): List<Task> =
        selectFromTasks()
            .where(TASKS.IS_ACTUAL.eq(true))
            .orderBy(TASKS.ID)
            .fetch()
            .mapNotNull { record: Record ->
                record.toTask()
            }

    override fun selectAllTasksIdAndName(): List<TasksListData> =
        jooqContext
            .select(
                TASKS.ID,
                TASKS.NAME
            ).from(TASKS)
            .where(TASKS.IS_ACTUAL.eq(true))
            .fetch()
            .mapNotNull { record: Record -> record.toTasksListData() }

    override fun selectHiddenTasksIdAndName(): List<TasksListData> =
        jooqContext
            .select(
                TASKS.ID,
                TASKS.NAME
            ).from(TASKS)
            .where(TASKS.IS_ACTUAL.eq(false))
            .fetch()
            .mapNotNull { record: Record -> record.toTasksListData() }

    override fun selectTaskName(taskId: Int): TaskNameForAllResults? =
        jooqContext
            .select(
                TASKS.NAME
            ).from(TASKS)
            .where(TASKS.ID.eq(taskId))
            .fetchOne()
            ?.let { record: Record -> record.toTaskDataForAllResults() }

    override fun insertTask(
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ): Task? {
        return jooqContext.insertInto(TASKS)
            .set(TASKS.NAME, name)
            .set(TASKS.CRITERIONS, jsonb(objectMapper.writeValueAsString(criterions)))
            .set(TASKS.ANSWERFORMAT, jsonb(objectMapper.writeValueAsString(answerFormat)))
            .set(TASKS.DESCRIPTION, description)
            .set(TASKS.IS_ACTUAL, isActual)
            .returningResult()
            .fetchOne()
            ?.toTask()
    }

    override fun deleteTask(taskId: Int): Int {
        var deleteTaskFlag = 0
        jooqContext.transaction { _ ->
            deleteSolutions(taskId)
            deleteTaskFlag = jooqContext.delete(TASKS)
                .where(TASKS.ID.eq(taskId))
                .execute()
            when {
                deleteTaskFlag == 1 -> commit()
                else -> rollback()
            }
        }
        return deleteTaskFlag
    }

    override fun updateTaskActuality(task: Task): Task? {
        return jooqContext.update(TASKS)
            .set(TASKS.IS_ACTUAL, task.isActual)
            .where(TASKS.ID.eq(task.id))
            .returningResult()
            .fetchOne()
            ?.toTask()
    }

    private fun deleteSolutions(taskId: Int): Int =
        jooqContext.delete(CHECKS)
            .where(CHECKS.TASKID.eq(taskId))
            .execute()

    private fun selectFromTasks() =
        jooqContext
            .select(
                TASKS.ID,
                TASKS.NAME,
                TASKS.CRITERIONS,
                TASKS.ANSWERFORMAT,
                TASKS.DESCRIPTION,
                TASKS.IS_ACTUAL
            )
            .from(TASKS)
}

internal fun Record.toTask(): Task? =
    safeLet(
        this[TASKS.ID],
        this[TASKS.NAME],
        this[TASKS.CRITERIONS],
        this[TASKS.ANSWERFORMAT],
        this[TASKS.DESCRIPTION],
        this[TASKS.IS_ACTUAL]
    ) {
            id,
            name,
            criterions,
            answerFormat,
            description,
            isActual,
        ->
        Task(
            id = id,
            name = name,
            criterions = jacksonObjectMapper().readValue<Map<String, Criterion>>(criterions.data()),
            answerFormat = jacksonObjectMapper().readValue<Map<String, AnswerType>>(answerFormat.data()),
            description = description,
            isActual = isActual
        )
    }

internal fun Record.toTaskDataForAllResults(): TaskNameForAllResults? =
    safeLet(
        this[TASKS.NAME]
    ) {
            name,
        ->
        TaskNameForAllResults(
            name = name,
        )
    }

internal fun Record.toTasksListData(): TasksListData? =
    safeLet(
        this[TASKS.ID],
        this[TASKS.NAME]
    ) {
            id,
            name,
        ->
        TasksListData(
            id = id.toString(),
            name = name,
        )
    }
