import Cassandra.CassandraConnection
import User.User
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.cql.{ResultSet, Row, SimpleStatement}
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder
import com.datastax.oss.driver.api.querybuilder.QueryBuilder._
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import com.datastax.oss.driver.api.querybuilder.relation.Relation.column
import com.datastax.oss.driver.api.querybuilder.select.Select

import java.time.LocalDate
import java.util.{Comparator, UUID}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Success, Try}

object Article {

  val ARTICLE_TABLE = "ARTICLE"

  case class Article(
                    articleID: Option[UUID],
                      title: String,
                      description: String,
                      linkArticle: String,
                      pubDate: LocalDate,
                      guid: String,
                      linkFlux: String
                    ) {

    def insert(cassandraConnection: CassandraConnection): Unit = {
      val baseQuery: RegularInsert =
        insertInto(ARTICLE_TABLE)
          .value("articleID", literal(UUID.randomUUID()))
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

    def createAndInsertArticle(title: String, description: String, linkArticle: String, pubDate: LocalDate, guid: String, linkFlux: String)(cassandraConnection: CassandraConnection): Article = {
      val article = Article(
        articleID = None,
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

    def fromCassandra(row: Row): Try[Article] =
      Try(
        Article(
          articleID = Some(row.getUuid("articleID")),
          title = row.getString("title"),
          description = row.getString("description"),
          linkArticle = row.getString("linkArticle"),
          pubDate = row.getLocalDate("pubDate"),
          guid = row.getString("guid"),
          linkFlux = row.getString("linkFlux")
        )
      )

    def createTable(cassandraConnection: CassandraConnection): Unit = {
      val query =
        SchemaBuilder
          .createTable(ARTICLE_TABLE)
          .ifNotExists()
          .withPartitionKey("articleID", DataTypes.UUID)
          .withColumn("title", DataTypes.TEXT)
          .withColumn("description", DataTypes.TEXT)
          .withColumn("linkArticle", DataTypes.TEXT)
          .withClusteringColumn("pubDate", DataTypes.DATE)
          .withColumn("guid", DataTypes.TEXT)
          .withColumn("linkFlux", DataTypes.TEXT)
          .withClusteringOrder("pubDate", ClusteringOrder.ASC)

      cassandraConnection.execute(query.build)
    }

    private def retrieve(query: Select)(cassandraConnection: CassandraConnection): List[Article] = {
      val result: ResultSet = cassandraConnection.execute(query.build)
      result.all().asScala.toList.map(fromCassandra).collect {case Success(v) => v}
    }

    def retrieveById(id: UUID)(cassandraConnection: CassandraConnection): Option[Article] = {
      val query =
        selectFrom(ARTICLE_TABLE)
          .all()
          .where(column("articleId").isEqualTo(literal(id)))
      retrieve(query)(cassandraConnection).headOption
    }

    def retrieveLastTenArticles(user: User)(cassandraConnection: CassandraConnection): List[Article] = {
      val list : List[UUID] = user.abonnement

      val listFlux : List[String] =
        list.map(id => Abonnement.Abonnement.retrieveById(id)(cassandraConnection))
            .map(option => option.get)
            .map(abonnement => abonnement.flux)

      listFlux.flatMap(flux => {
          val query =
            selectFrom(ARTICLE_TABLE)
              .all()
              .where(column("linkFlux")
                .isEqualTo(literal(flux)))
              .limit(10).allowFiltering()
          retrieve(query)(cassandraConnection)
        }).sortBy(_.pubDate).take(10)
    }
  }
}
