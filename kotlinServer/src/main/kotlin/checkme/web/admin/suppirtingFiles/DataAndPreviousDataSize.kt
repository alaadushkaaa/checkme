package checkme.web.admin.suppirtingFiles

data class DataAndPreviousDataSize(
    val emailAndLogins: List<Pair<String, String>>,
    val previousSize: Int,
)
