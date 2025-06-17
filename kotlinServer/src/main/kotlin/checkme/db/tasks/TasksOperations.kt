package checkme.db.tasks

import checkme.db.generated.tables.references.TASKS
import checkme.db.utils.safeLet
import checkme.domain.checks.Criterion
import checkme.domain.models.Task
import checkme.domain.operations.dependencies.TasksDatabase
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.JSONB.jsonb
import org.jooq.Record

class TasksOperations (
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
            .fetch()
            .mapNotNull { record: Record ->
                record.toTask()
            }

    override fun insertTask(
        taskId: Int,
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: String,
        description: String
    ): Task? {
        return jooqContext.insertInto(TASKS)
            .set(TASKS.NAME, name)
            .set(TASKS.CRITERIONS, jsonb(objectMapper.writeValueAsString(criterions)))
            .set(TASKS.ANSWERFORMAT, answerFormat)
            .set(TASKS.DESCRIPTION, description)
            .returningResult()
            .fetchOne()
            ?.toTask()
    }

    private fun selectFromTasks() =
        jooqContext
            .select(
                TASKS.ID,
                TASKS.NAME,
                TASKS.CRITERIONS,
                TASKS.ANSWERFORMAT,
                TASKS.DESCRIPTION
            )
            .from(TASKS)
}

internal fun Record.toTask(): Task? =
    safeLet(
        this[TASKS.ID],
        this[TASKS.NAME],
        this[TASKS.CRITERIONS],
        this[TASKS.ANSWERFORMAT],
        this[TASKS.DESCRIPTION]
    ) {
            id,
            name,
            criterions,
            answerFormat,
            description,
        ->
        Task(
            id = id,
            name = name,
            criterions = jacksonObjectMapper().readValue<Map<String, Criterion>>(criterions.data()),
            answerFormat = answerFormat,
            description = description
        )
    }