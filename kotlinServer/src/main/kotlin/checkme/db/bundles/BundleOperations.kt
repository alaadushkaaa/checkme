package checkme.db.bundles

import checkme.db.generated.tables.references.BUNDLES
import checkme.db.generated.tables.references.BUNDLE_TASKS
import checkme.db.tasks.TasksOperations
import checkme.db.utils.safeLet
import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndPriority
import checkme.domain.operations.dependencies.bundles.BundleDatabaseError
import checkme.domain.operations.dependencies.bundles.BundlesDatabase
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.exception.DataAccessException
import org.jooq.exception.IntegrityConstraintViolationException
import org.jooq.impl.DSL.commit
import org.jooq.impl.DSL.rollback

@Suppress("TooManyFunctions")
class BundleOperations(
    private val jooqContext: DSLContext,
    private val taskOperations: TasksOperations,
) : BundlesDatabase {

    override fun selectBundleById(bundleId: Int): Bundle? =
        selectFromBundles()
            .where(BUNDLES.ID.eq(bundleId))
            .fetchOne()
            ?.toBundle()

    override fun selectAllBundles(): List<Bundle> =
        selectFromBundles()
            .where(BUNDLES.ISACTUAL.eq(true))
            .fetch()
            .mapNotNull { record: Record ->
                record.toBundle()
            }

    override fun selectHiddenBundles(): List<Bundle> =
        selectFromBundles()
            .where(BUNDLES.ISACTUAL.eq(false))
            .fetch()
            .mapNotNull { record: Record ->
                record.toBundle()
            }

    override fun selectBundleTasksById(id: Int): List<TaskAndPriority> = selectBundleTasksByIDRecords(id)

    override fun insertBundle(name: String): Bundle? {
        return jooqContext.insertInto(BUNDLES)
            .set(BUNDLES.NAME, name)
            .set(BUNDLES.ISACTUAL, true)
            .returningResult()
            .fetchOne()
            ?.toBundle()
    }

    override fun insertBundleTasks(
        bundleId: Int,
        tasksAndPriority: List<TaskAndPriority>,
    ): List<TaskAndPriority>? {
        var savedTasks: List<TaskAndPriority>? = null
        jooqContext.transaction { _ ->
            savedTasks = setTasks(bundleId, tasksAndPriority)
            when {
                savedTasks != null -> commit()
                else -> rollback()
            }
        }

        return savedTasks
    }

    override fun updateBundleActuality(bundle: Bundle): Bundle? {
        return jooqContext.update(BUNDLES)
            .set(BUNDLES.ISACTUAL, bundle.isActual)
            .where(BUNDLES.ID.eq(bundle.id))
            .returningResult()
            .fetchOne()
            ?.toBundle()
    }

    override fun updateBundle(bundle: Bundle): Bundle? {
        return jooqContext.update(BUNDLES)
            .set(BUNDLES.NAME, bundle.name)
            .set(BUNDLES.ISACTUAL, bundle.isActual)
            .where(BUNDLES.ID.eq(bundle.id))
            .execute()
            .let { selectBundleById(bundle.id) }
    }

    override fun updateBundleTasks(
        bundleId: Int,
        newTasksAndPriority: List<TaskAndPriority>,
    ): List<TaskAndPriority>? {
        var savedTasks: List<TaskAndPriority>? = null
        jooqContext.transaction { _ ->
            jooqContext.deleteFrom(BUNDLE_TASKS)
                .where(BUNDLE_TASKS.BUNDLE_ID.eq(bundleId))
                .execute()

            savedTasks = setTasks(bundleId, newTasksAndPriority)
            when {
                savedTasks != null -> commit()
                else -> rollback()
            }
        }

        return savedTasks
    }

    override fun deleteBundle(bundleId: Int): Result4k<Boolean, BundleDatabaseError> {
        var result: Result4k<Boolean, BundleDatabaseError> = Failure(BundleDatabaseError.UNKNOWN_DATABASE_ERROR)
        jooqContext.transaction { _ ->
            try {
                jooqContext.deleteFrom(BUNDLE_TASKS)
                    .where(BUNDLE_TASKS.BUNDLE_ID.eq(bundleId))
                    .execute()
                jooqContext.deleteFrom(BUNDLES)
                    .where(BUNDLES.ID.eq(bundleId))
                    .execute()
                commit()
                result = Success(true)
            } catch (_: DataAccessException) {
                rollback()
            } catch (_: IntegrityConstraintViolationException) {
                rollback()
            }
        }
        return result
    }

    private fun selectBundleTasksByIDRecords(id: Int) =
        jooqContext.select(
            BUNDLE_TASKS.TASK_ID,
            BUNDLE_TASKS.PRIORITY,
        ).from(BUNDLE_TASKS)
            .where(BUNDLE_TASKS.BUNDLE_ID.eq(id))
            .fetch()
            .map {
                it.toTaskAndPriority(taskOperations)
            }

    private fun setTasks(
        bundleId: Int,
        tasksAndPriority: List<TaskAndPriority>,
    ): List<TaskAndPriority>? =
        tasksAndPriority.map { (task, priority) ->
            jooqContext
                .insertInto(BUNDLE_TASKS)
                .set(BUNDLE_TASKS.BUNDLE_ID, bundleId)
                .set(BUNDLE_TASKS.TASK_ID, task.id)
                .set(BUNDLE_TASKS.PRIORITY, priority)
                .returningResult()
                .fetchOne()
                ?.toTaskAndPriority(taskOperations) ?: return null
        }

    private fun selectFromBundles() =
        jooqContext
            .select(
                BUNDLES.ID,
                BUNDLES.NAME,
                BUNDLES.ISACTUAL
            )
            .from(BUNDLES)
}

internal fun Record.toBundle(): Bundle? =
    safeLet(
        this[BUNDLES.ID],
        this[BUNDLES.NAME],
        this[BUNDLES.ISACTUAL],
    ) {
            id,
            name,
            isActual,
        ->
        Bundle(
            id = id,
            name = name,
            isActual = isActual
        )
    }

internal fun Record.toTaskAndPriority(taskOperations: TasksOperations): TaskAndPriority? =
    safeLet(
        this[BUNDLE_TASKS.TASK_ID],
        this[BUNDLE_TASKS.PRIORITY],
    ) { taskID, priority ->
        taskOperations.selectTaskById(taskID)?.let { TaskAndPriority(it, priority) }
    }
