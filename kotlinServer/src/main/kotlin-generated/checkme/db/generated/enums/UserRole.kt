/*
 * This file is generated by jOOQ.
 */
package checkme.db.generated.enums


import checkme.db.generated.Public

import javax.annotation.processing.Generated

import org.jooq.Catalog
import org.jooq.EnumType
import org.jooq.Schema


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = [
        "https://www.jooq.org",
        "jOOQ version:3.19.10",
        "catalog version:02",
        "schema version:02"
    ],
    comments = "This class is generated by jOOQ"
)
@Suppress("UNCHECKED_CAST")
enum class UserRole(@get:JvmName("literal") public val literal: String) : EnumType {
    ADMIN("ADMIN"),
    STUDENT("STUDENT");
    override fun getCatalog(): Catalog? = schema.catalog
    override fun getSchema(): Schema = Public.PUBLIC
    override fun getName(): String = "user_role"
    override fun getLiteral(): String = literal
}
