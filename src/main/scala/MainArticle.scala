import Article.Article

import java.time.LocalDate

object MainArticle {

  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Article.createTable(connection)
    Abonnement.Abonnement.createTable(connection)
    User.User.createTable(connection)

    Article.createAndInsertArticle(
      "Test",
      "Ceci est un article test",
      "https://fluxTest.fr/articleTest",
      LocalDate.now(),
      "https://fluxTest1.fr",
      "https://fluxTest1.fr"
    )(connection)

    Article.createAndInsertArticle(
      "newTest",
      "Ceci est un 2e article test",
      "https://fluxTest.fr/articleTest2",
      LocalDate.now(),
      "https://fluxTest.fr",
      "https://fluxTest.fr"
    )(connection)

    val abonnement = Abonnement.Abonnement.createAbonnement("https://fluxTest.fr")(connection)
    val abonnement2 = Abonnement.Abonnement.createAbonnement("https://fluxTest1.fr")(connection)
    val user = User.User.createUser(List(abonnement.idAbonnement.get, abonnement2.idAbonnement.get))(connection)


    println(Article.retrieveLastTenArticles(user)(connection))
  }
}
