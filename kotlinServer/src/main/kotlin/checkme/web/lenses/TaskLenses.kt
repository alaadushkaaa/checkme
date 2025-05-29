package checkme.web.lenses

import org.http4k.lens.Path
import org.http4k.lens.int

object TaskLenses {
    val taskIdPathField = Path.int().of("taskId")
}
