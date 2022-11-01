import Cassandra.CassandraConnection
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.`type`.codec.registry.CodecRegistry
import com.datastax.oss.driver.api.core.cql.{ResultSet, Row, SimpleStatement}
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.{insertInto, literal, selectFrom}
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import com.datastax.oss.driver.api.querybuilder.relation.Relation.column
import com.datastax.oss.driver.api.querybuilder.select.Select
import com.sun.jna.platform.win32.WinDef.LONG

import java.math.BigInteger
import java.util
import scala.collection.JavaConverters.seqAsJavaList
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Success, Try}

object User {
  val USER_TABLE = "USER"
  case class User(
                   idUser: Long,
                   abonnement: List[Long]
                 ) {
    def insert(cassandraConnection: CassandraConnection): Unit = {
      val baseQuery: RegularInsert =
        insertInto(USER_TABLE)
          .value("id", literal(idUser))
          .value("abonnement", literal(seqAsJavaList(abonnement)))

      val statement: SimpleStatement = baseQuery.build
      cassandraConnection.execute(statement)
    }
  }

  def fromCassandra(row: Row): Try[User] =
    Try(
      User(
        idUser = row.getLong("id"),
        abonnement = row.getList("abonnement", classOf[java.lang.Long]).asScala.toList.map(Long.unbox)
      )
    )

  def createTableById(cassandraConnection: CassandraConnection): Unit = {
    val query =
      SchemaBuilder
        .createTable(USER_TABLE)
        .ifNotExists()
        .withPartitionKey("id", DataTypes.BIGINT)
        .withColumn("abonnement", DataTypes.listOf(DataTypes.BIGINT))
    val statement: SimpleStatement = query.build
    cassandraConnection.execute(statement)
  }

  private def retrieve(query: Select)(cassandraConnection: CassandraConnection): List[User] = {
    val statement = query.build
    val result: ResultSet = cassandraConnection.execute(statement)

    result.all().asScala.toList.map(fromCassandra).collect { case Success(v) => v }
  }

  def retrieveById(id: Long)(cassandraConnection: CassandraConnection): User = {
    val query =
      selectFrom(USER_TABLE)
        .all()
        .where(column("id").isEqualTo(literal(id)))
    retrieve(query)(cassandraConnection).last
  }

  def retrieveListAbonnementById(id: Long)(cassandraConnection: CassandraConnection): List[Abonnement.Abonnement] = {
    val user = retrieveById(id)(cassandraConnection)
    user.abonnement.map(abonnementId => Abonnement.Data.retrieveById(abonnementId)(cassandraConnection))
  }
}
