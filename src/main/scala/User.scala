import Cassandra.CassandraConnection
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.`type`.codec.registry.CodecRegistry
import com.datastax.oss.driver.api.core.cql.{ResultSet, Row, SimpleStatement}
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.{insertInto, literal, now, selectFrom}
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import com.datastax.oss.driver.api.querybuilder.relation.Relation.column
import com.datastax.oss.driver.api.querybuilder.select.Select
import com.sun.jna.platform.win32.WinDef.LONG

import java.math.BigInteger
import java.util
import java.util.UUID
import scala.collection.JavaConverters.seqAsJavaList
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Success, Try}

object User {
  val USER_TABLE = "USER"

  def createUser(list: List[UUID])(cassandraConnection: CassandraConnection): User ={
    val user = User(
      idUser = Option(UUID.randomUUID()),
      abonnement = list
    )
    user.insert(cassandraConnection)
    user
  }
  case class User(
                   idUser: Option[UUID],
                   abonnement: List[UUID]
                 ) {
    def insert(cassandraConnection: CassandraConnection): Unit = {
      val baseQuery: RegularInsert =
        insertInto(USER_TABLE)
          .value("id", literal(idUser.get))
          .value("abonnement", literal(seqAsJavaList(abonnement)))

      val statement: SimpleStatement = baseQuery.build
      cassandraConnection.execute(statement)
    }
  }

  def fromCassandra(row: Row): Try[User] =
    Try(
      User(
        idUser = Some(row.getUuid("id")),
        abonnement = row.getList("abonnement", classOf[UUID]).asScala.toList
      )
    )

  def createTableById(cassandraConnection: CassandraConnection): Unit = {
    val query =
      SchemaBuilder
        .createTable(USER_TABLE)
        .ifNotExists()
        .withPartitionKey("id", DataTypes.UUID)
        .withColumn("abonnement", DataTypes.listOf(DataTypes.UUID))
    val statement: SimpleStatement = query.build
    cassandraConnection.execute(statement)
  }

  private def retrieve(query: Select)(cassandraConnection: CassandraConnection): List[User] = {
    val statement = query.build
    val result: ResultSet = cassandraConnection.execute(statement)

    result.all().asScala.toList.map(fromCassandra).collect { case Success(v) => v }
  }

  def retrieveById(id: UUID)(cassandraConnection: CassandraConnection): User = {
    val query =
      selectFrom(USER_TABLE)
        .all()
        .where(column("id").isEqualTo(literal(id)))
    retrieve(query)(cassandraConnection).last
  }

  def retrieveListAbonnementByUser(user: User)(cassandraConnection: CassandraConnection): List[Abonnement.Abonnement] = {
    user.abonnement.map(abonnementId => Abonnement.retrieveById(abonnementId)(cassandraConnection))
  }
}
