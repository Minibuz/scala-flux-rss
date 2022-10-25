import Cassandra.CassandraConnection
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.cql.{ResultSet, Row, SimpleStatement}
import com.datastax.oss.driver.api.querybuilder.QueryBuilder._
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import com.datastax.oss.driver.api.querybuilder.relation.Relation._
import com.datastax.oss.driver.api.querybuilder.select.Select

import java.util.Date
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Success, Try}

object Article {

  val RSS_TABLE = "RSS"

  case class Article(
                      articleId: String,
                      title: String,
                      description: String,
                      linkArticle: String,
                      pubDate: Date,
                      guid: String,
                      linkFlux: String
                    ) {

    def insert(tableName: String)(cassandraConnection: CassandraConnection): Unit = {
      val baseQuery: RegularInsert =
        insertInto(tableName)
          .value("id", literal(id))

      val statement: SimpleStatement = baseQuery.build
      cassandraConnection.execute(statement)
    }
  }

  object Data {

    def fromCassandra(row: Row): Try[Article] =
      Try(
        Article(
          id = row.getString("id")
        )
      )

    def createTableById(cassandraConnection: CassandraConnection): Unit = {
      val query =
        SchemaBuilder
          .createTable(RSS_TABLE)
          .ifNotExists()
          .withPartitionKey("id", DataTypes.TEXT)

      val statement: SimpleStatement = query.build
      cassandraConnection.execute(statement)
    }

    private def retrieve(query: Select)(cassandraConnection: CassandraConnection): List[Article] = {
      val statement = query.build
      val result: ResultSet = cassandraConnection.execute(statement)

      result.all().asScala.toList.map(fromCassandra).collect { case Success(v) => v }
    }

    def retrieveById(id: Int)(cassandraConnection: CassandraConnection): List[Article] = {
      val query =
        selectFrom(RSS_TABLE)
          .all()
          .where(column("id").isEqualTo(literal(id)))
      retrieve(query)(cassandraConnection)
    }
  }

  case class FluxRss(
                       id: String
                     ) {

  }

  case class User(
                      id: String
                    ) {

  }
}
