import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

fun main(args: Array<String>) {
    println("Hello World!")
}

object PetTable : Table(name = "pet") {
    val id = uuid("id_id")
    val name = varchar("name", 50)


    suspend fun createPet(
        id: UUID,
        name: String,
    ): InsertStatement<Number> {
        return newSuspendedTransaction {
            addLogger(StdOutSqlLogger)
            PetTable.insert {
                it[this.id] = id
                it[this.name] = name
            }
        }
    }

    suspend fun findPetByID(
        id: UUID,
    ): String? {
        return newSuspendedTransaction {
            addLogger(StdOutSqlLogger)
            PetTable.select { PetTable.id eq id }.map { it[PetTable.name] }.singleOrNull()
        }
    }
}
