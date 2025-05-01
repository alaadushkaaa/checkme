package checkme.domain.operations.dependencies

import checkme.domain.models.Check
import java.time.LocalDateTime


interface ChecksDatabase {
    fun selectCheckById(checkId: Int) : Check?

    fun selectChecksByUserId(userId: Int) : List<Check>

    fun selectAllChecks() : List<Check>

    fun insertCheck(
        userId: Int,
        date: LocalDateTime
    )
}