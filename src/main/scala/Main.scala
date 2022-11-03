import Article.Article
import spark.Spark._
import spark.{Request, Response}

object Main {
  val serverPort = 8090

  def main(args: Array[String]): Unit = {

    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

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

        val name = request.params("article_id")

        println(name)

        s"""{"message": "article id = $name"}"""
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