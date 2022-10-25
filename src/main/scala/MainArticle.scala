import Article.{Article, Data}

import java.time.LocalDate
import scala.util.Random

object MainArticle {
  val serverPort = 8090

  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Data.createTable(connection)

     val article: Article = {
       Article.newArticle(
         "Test",
         "Ceci est un article test",
         "https://fluxTest.fr/articleTest",
         LocalDate.now(),
         Random.nextLong(),
         "https://fluxTest.fr"
       )(connection)
     }

    val article1: Article = {
      Article.newArticle(
        "newTest",
        "Ceci est un 2e article test",
        "https://fluxTest.fr/articleTest2",
        LocalDate.now(),
        Random.nextLong(),
        "https://fluxTest.fr"
      )(connection)
    }

    val listArticle: List[Article] = Data.retrieveById(article.articleId)(connection)
    val listArticle2: List[Article] = Data.retrieveById(article1.articleId)(connection)

    for(articleTest <- listArticle) {
      println(articleTest)
    }
    for (articleTest2 <- listArticle2) {
      println(articleTest2)
    }
  }
}
