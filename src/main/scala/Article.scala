import Cassandra.CassandraConnection
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.cql.{ResultSet, Row, SimpleStatement}
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder
import com.datastax.oss.driver.api.querybuilder.QueryBuilder._
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import com.datastax.oss.driver.api.querybuilder.relation.Relation._
import com.datastax.oss.driver.api.querybuilder.select.Select

import java.time.LocalDate
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success, Try}

object Article {

  val ARTICLE_TABLE = "ARTICLE"

  case class Article(
                      articleId: Long,
                      title: String,
                      description: String,
                      linkArticle: String,
                      pubDate: LocalDate,
                      guid: Long,
                      linkFlux: String
                    ) {

    def insert(cassandraConnection: CassandraConnection): Unit = {
      val baseQuery: RegularInsert =
        insertInto(ARTICLE_TABLE)
          .value("articleId", literal(articleId))
          .value("title", literal(title))
          .value("description", literal(description))
          .value("linkArticle", literal(linkArticle))
          .value("pubDate", literal(pubDate))
          .value("guid", literal(guid))
          .value("linkFlux", literal(linkFlux))

      val statement: SimpleStatement = baseQuery.build
      cassandraConnection.execute(statement)
    }
  }

  object Article {
    def newArticle(title: String, description: String, linkArticle: String, pubDate: LocalDate, guid: Long, linkFlux: String)(cassandraConnection: CassandraConnection): Article = {
      val article = Article(
        articleId = cassandraConnection.getArticleId,
        title = title,
        description = description,
        linkArticle = linkArticle,
        pubDate = pubDate,
        guid = guid,
        linkFlux = linkFlux
      )
      article.insert(cassandraConnection)
      article
    }
  }
  object Data {

    def fromCassandra(row: Row): Try[Article] =
      Try(
        Article(
          articleId = row.getLong("articleId"),
          title = row.getString("title"),
          description = row.getString("description"),
          linkArticle = row.getString("linkArticle"),
          pubDate = row.getLocalDate("pubDate"),
          guid = row.getLong("guid"),
          linkFlux = row.getString("linkFlux")
        )
      )

    def createTable(cassandraConnection: CassandraConnection): Unit = {
      val query =
        SchemaBuilder
          .createTable(ARTICLE_TABLE)
          .ifNotExists()
          .withPartitionKey("articleID", DataTypes.BIGINT)
          .withColumn("title", DataTypes.TEXT)
          .withColumn("description", DataTypes.TEXT)
          .withColumn("linkArticle", DataTypes.TEXT)
          .withClusteringColumn("pubDate", DataTypes.DATE)
          .withColumn("guid", DataTypes.BIGINT)
          .withColumn("linkFlux", DataTypes.TEXT)
          .withClusteringOrder("pubDate", ClusteringOrder.DESC)

      cassandraConnection.execute(query.build)
    }

    private def retrieve(query: Select)(cassandraConnection: CassandraConnection): List[Article] = {
      val result: ResultSet = cassandraConnection.execute(query.build)
      result.all().asScala.toList.map(fromCassandra).collect { case Success(v) => v }
    }

    def retrieveById(id: Long)(cassandraConnection: CassandraConnection): List[Article] = {
      val query =
        selectFrom(ARTICLE_TABLE)
          .all()
          .where(column("articleId").isEqualTo(literal(id)))
      retrieve(query)(cassandraConnection)
    }
  }
}
