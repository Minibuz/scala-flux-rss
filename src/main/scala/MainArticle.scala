import Article.Article

import java.time.LocalDate
import java.util.UUID
import scala.util.Random

object MainArticle {
  val serverPort = 8090

  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Article.createTable(connection)
    Abonnement.createTableById(connection)
    User.createTableById(connection)

    Article.createAndInsertArticle(
      "Test",
      "Ceci est un article test",
      "https://fluxTest.fr/articleTest",
      LocalDate.now(),
      UUID.randomUUID(),
      "https://fluxTest1.fr"
    )(connection)

    Article.createAndInsertArticle(
      "newTest",
      "Ceci est un 2e article test",
      "https://fluxTest.fr/articleTest2",
      LocalDate.now(),
      UUID.randomUUID(),
      "https://fluxTest.fr"
    )(connection)

    val abonnement = Abonnement.createAbonnement("https://fluxTest.fr")(connection)
    val abonnement2 = Abonnement.createAbonnement("https://fluxTest1.fr")(connection)
    val user = User.createUser(List(abonnement.idAbonnement.get, abonnement2.idAbonnement.get))(connection)


    println(Article.retrieveLastTenArticles(user)(connection))
  }
}
