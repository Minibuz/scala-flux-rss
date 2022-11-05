import Cassandra.CassandraConnection
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.cql.{ResultSet, Row, SimpleStatement}
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.{insertInto, literal, selectFrom}
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import com.datastax.oss.driver.api.querybuilder.relation.Relation.column
import com.datastax.oss.driver.api.querybuilder.select.Select

import java.util.UUID
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Success, Try}

object Abonnement {
  val ABONNEMENT_TABLE = "ABONNEMENT"

  def createAbonnement(flux: String)(cassandraConnection: CassandraConnection): Abonnement = {
    val abonnement = Abonnement(
      idAbonnement = Option(UUID.randomUUID()),
      flux = flux
    )
    abonnement.insert(cassandraConnection)
    abonnement
  }

  case class Abonnement(
                         idAbonnement: Option[UUID],
                         flux: String
                       ) {

    def insert(cassandraConnection: CassandraConnection): Unit = {
      val baseQuery: RegularInsert =
        insertInto(ABONNEMENT_TABLE)
          .value("id", literal(idAbonnement.get))
          .value("flux", literal(flux))

      val statement: SimpleStatement = baseQuery.build
      cassandraConnection.execute(statement)
    }
  }

  def fromCassandra(row: Row): Try[Abonnement] =
    Try(
      Abonnement(
        idAbonnement = Some(row.getUuid("id")),
        flux = row.getString("flux")
      )
    )

  def createTable(cassandraConnection: CassandraConnection): Unit = {
    val query =
      SchemaBuilder
        .createTable(ABONNEMENT_TABLE)
        .ifNotExists()
        .withPartitionKey("id", DataTypes.UUID)
        .withColumn("flux", DataTypes.TEXT)
    val statement: SimpleStatement = query.build
    cassandraConnection.execute(statement)
  }
  private def retrieve(query: Select)(cassandraConnection: CassandraConnection): List[Abonnement] = {
    val statement = query.build
    val result: ResultSet = cassandraConnection.execute(statement)

    result.all().asScala.toList.map(fromCassandra).collect { case Success(v) => v }
  }

  def retrieveById(id: UUID)(cassandraConnection: CassandraConnection): Option[Abonnement] = {
    val query =
      selectFrom(ABONNEMENT_TABLE)
        .all()
        .where(column("id").isEqualTo(literal(id)))
    retrieve(query)(cassandraConnection).headOption
  }

  def retrieveByFlux(flux: String)(cassandraConnection: CassandraConnection): Abonnement = {
    val query =
      selectFrom(ABONNEMENT_TABLE)
        .all()
        .where(column("flux").isEqualTo(literal(flux))).allowFiltering()
    retrieve(query)(cassandraConnection).last
  }
}
