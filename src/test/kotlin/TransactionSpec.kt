import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.WordSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@OptIn(ExperimentalTime::class)
class TransactionSpec: WordSpec({

    val network = Network.newNetwork()
    val postgreSQLContainer = TestPostgresSQLContainer("postgres:9.6.20")
        .withDatabaseName("test")
        .withUsername("postgres")
        .withPassword("password")
        .withExposedPorts(5432)
        .withNetwork(network)

    listener(postgreSQLContainer.perSpec())

    "suspend transactions" should {
        "work concurrently across scopes" {
            val datasource = buildDatasource(postgreSQLContainer)
            TransactionManager.defaultDatabase = Database.connect(datasource, { println("getting connection $it") })
            transaction {
                SchemaUtils.createSchema(Schema("test"))
                SchemaUtils.createMissingTablesAndColumns(PetTable)
            }
            println("done with DDL, starting test")
            repeat(20) {
                runBlocking {
                    val id = UUID.randomUUID()
                    GlobalScope.launch {
                        PetTable.createPet(id, System.currentTimeMillis().toString())
                    }
                    eventually(2.seconds) {
                        PetTable.findPetByID(id) shouldNotBe null
                    }
                }
            }
        }
    }

})

fun buildDatasource(postgresContainer: TestPostgresSQLContainer): HikariDataSource {
    val config = HikariConfig()

    config.jdbcUrl = "jdbc:postgresql://${postgresContainer.containerIpAddress}:${postgresContainer.firstMappedPort}/test"
    config.username = "postgres"
    config.password = "password"
    config.schema = "test"
    config.leakDetectionThreshold = 4000
    config.isAutoCommit = false
    config.maximumPoolSize = 5

    return HikariDataSource(config)
}

class TestPostgresSQLContainer(image: String): PostgreSQLContainer<TestPostgresSQLContainer>(image)