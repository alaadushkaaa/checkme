package checkme.db.checks

import checkme.db.generated.tables.references.CHECKS
import checkme.db.utils.safeLet
import checkme.domain.models.Check
import checkme.domain.operations.dependencies.ChecksDatabase
import org.jooq.DSLContext
import java.time.LocalDateTime

class CheckOperations (
    private val jooqContext: DSLContext,
) : ChecksDatabase {
    override fun selectAllChecks(): List<Check> {
        TODO("Not yet implemented")
    }

    override fun selectCheckById(checkId: Int): Check? {
        TODO("Not yet implemented")
    }

    override fun selectChecksByUserId(userId: Int): List<Check> {
        TODO("Not yet implemented")
    }

    override fun updateCheckStatus(status: String): Check? {
        TODO("Not yet implemented")
    }

    override fun insertCheck(userId: Int, date: LocalDateTime, status: String): Check? {
        TODO("Not yet implemented")
    }
}

internal fun Record.toCheck() : Check? =
    safeLet(
        this[CHECKS.ID]
    )
