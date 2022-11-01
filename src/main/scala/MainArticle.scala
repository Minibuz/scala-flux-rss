import Article.Article
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.now
import org.apache.cassandra.db.marshal.TimeUUIDType

import java.time.LocalDate
import scala.util.Random

object MainArticle {
  val serverPort = 8090

  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Article.createTable(connection)

    Article.createAndInsertArticle(
      "Test",
      "Ceci est un article test",
      "https://fluxTest.fr/articleTest",
      LocalDate.now(),
      Random.nextLong(),
      "https://fluxTest.fr"
    )(connection)

    Article.createAndInsertArticle(
      "newTest",
      "Ceci est un 2e article test",
      "https://fluxTest.fr/articleTest2",
      LocalDate.now(),
      Random.nextLong(),
      "https://fluxTest.fr"
    )(connection)

    println(Article.retrieveLastTenArticles()(connection))
  }
}
