import Article.Article
import Abonnement.Abonnement
import spark.Spark._
import spark.{Request, Response}

import java.util.UUID
import scala.util.Failure

object Main {
  val serverPort = 8090

  def main(args: Array[String]): Unit = {

    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Article.createTable(connection)
    Abonnement.createTable(connection)

    port(serverPort)

    get(
      "/articles", "application/json",
      { (request: Request, response: Response) =>
        println(request.queryParams("user_id"))
        response.`type`("application/json")

        """{"message": "tous les articles"}"""
      }
    )

    get(
      "/articles/:article_id",
      { (request: Request, response: Response) =>
        response.`type`("application/json")

        val message = for {
          id <- Option(request.params("article_id"))
          // Faudrait peut être fix le UUID.fromString, car il peut péter une erreur
          article <- Article.retrieveById(UUID.fromString(id))(connection)
          msg = s"""$article"""
        } yield msg

        message.getOrElse(s"""no article for that id""")
      }
    )

    post(
      "/articles",
      { (request: Request, response: Response) =>
        response.`type`("application/json")

        val content =
          Option(
            request.queryParams("content")
          ).getOrElse("")

        s"""{"message": "$content"}"""
      }
    )

    get(
      "/wrong",
      { (request: Request, response: Response) =>
        response.`type`("application/json")
        response.status(404)

        s"""{"message": "Not Found"}"""
      }
    )
  }

  def findLast10ArticleSummaries(user_id: String) : List[Article] = {
    List.empty
  }

//  def findOneArticle(article_id: String) : Option[Article] = {
//    Some(Article(article_id))
//  }

  def saveArticles(articles: List[Article]) : Unit = {

  }
}