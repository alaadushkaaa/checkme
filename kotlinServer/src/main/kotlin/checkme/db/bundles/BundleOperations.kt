package checkme.db.bundles

import checkme.db.generated.tables.references.BUNDLES
import checkme.db.utils.safeLet
import checkme.domain.models.Bundle
import checkme.domain.operations.dependencies.bundles.BundlesDatabase
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jooq.DSLContext
import org.jooq.JSONB.jsonb
import org.jooq.Record

class BundlesOperations(
    private val jooqContext: DSLContext,
) : BundlesDatabase {
    private val objectMapper = jacksonObjectMapper()

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

    override fun insertBundle(
        name: String,
        tasks: Map<Int, Int>,
    ): Bundle? {
        return jooqContext.insertInto(BUNDLES)
            .set(BUNDLES.NAME, name)
            .set(BUNDLES.TASKS, jsonb(objectMapper.writeValueAsString(tasks)))
            .set(BUNDLES.ISACTUAL, true)
            .returningResult()
            .fetchOne()
            ?.toBundle()
    }

    override fun selectHiddenBundles(): List<Bundle> =
        selectFromBundles()
            .where(BUNDLES.ISACTUAL.eq(false))
            .fetch()
            .mapNotNull { record: Record ->
                record.toBundle()
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
            .set(BUNDLES.TASKS, jsonb(objectMapper.writeValueAsString(bundle.tasks)))
            .set(BUNDLES.ISACTUAL, bundle.isActual)
            .where(BUNDLES.ID.eq(bundle.id))
            .execute()
            .let { selectBundleById(bundle.id) }
    }

    override fun deleteBundle(bundleId: Int): Int {
        return jooqContext.deleteFrom(BUNDLES)
            .where(BUNDLES.ID.eq(bundleId))
            .execute()
    }

    private fun selectFromBundles() =
        jooqContext
            .select(
                BUNDLES.ID,
                BUNDLES.NAME,
                BUNDLES.TASKS,
                BUNDLES.ISACTUAL
            )
            .from(BUNDLES)
}

internal fun Record.toBundle(): Bundle? =
    safeLet(
        this[BUNDLES.ID],
        this[BUNDLES.NAME],
        this[BUNDLES.TASKS],
        this[BUNDLES.ISACTUAL],
    ) {
            id,
            name,
            tasks,
            isActual,
        ->
        Bundle(
            id = id,
            name = name,
            tasks = jacksonObjectMapper().readValue<Map<Int, Int>>(tasks.data()),
            isActual = isActual
        )
    }
