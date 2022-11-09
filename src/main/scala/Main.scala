import Article.Article
import Abonnement.Abonnement
import User.User
import io.circe._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import spark.Spark._
import spark.{Request, Response}

import java.time.LocalDate
import java.util.UUID

object Main {
  val serverPort = 8090

  def main(args: Array[String]): Unit = {

    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Article.createTable(connection)
    Abonnement.createTable(connection)
    User.createTable(connection)

    port(serverPort)

    get(
      "/articles", "application/json",
      { (request: Request, response: Response) =>
        response.`type`("application/json")

        val articles : Option[List[Article]] = for {
          id <- Option(request.queryParams("user_id"))
          // Faudrait peut être fix le UUID.fromString, car il peut péter une erreur
          user <- User.retrieveById(UUID.fromString(id))(connection)
          articles = Article.retrieveLastTenArticles(user)(connection)
        } yield articles

        articles match {
          case None => {
            response.status(418)
            s"""Not found"""
          }
          case Some(v) => s"""$v"""
        }
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

        message match {
          case None => {
            response.status(418)
            s"""Not found"""
          }
          case Some(v) => s"""$v"""
        }
      }
    )

    get(
      "/users",
      { (request: Request, response: Response) =>
        response.`type`("application/json")

        val users = Injection.injectDatas(connection)
        s"""$users"""
      }
    )

    post(
      "/articles",
      { (request: Request, response: Response) =>
        response.`type`("application/json")

        val json = request.body()
        val articles = parseJson(json)
        articles.foreach(article => article.insert(connection))

        s"""{"$articles"}"""
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

  def parseJson(entry: String) : List[Article] = {
    val parseResult: Either[ParsingFailure, Json] = parse(entry)

    val list = parseResult match {
      case Left(parsingError) =>
        throw new IllegalArgumentException(s"Invalid JSON object: ${parsingError.message}")
      case Right(json) =>
        json.as[List[Map[String, String]]] match {
          case Left(value) =>
            throw new IllegalArgumentException(s"Invalid JSON object: ${value}")
          case Right(value) =>
            value
        }
    }

    val articles = list.map(map => {
      for {
        title     <-  map.get("title")
        desc      <-  map.get("description")
        linkArt   <-  map.get("linkArticle")
        pubDate   <-  map.get("pubDate").map(str => LocalDate.parse(str))
        guid      <-  map.get("guid")
        linkFlux  <-  map.get("linkFlux")
        article = Article(None, title, desc, linkArt, pubDate, guid, linkFlux)
      } yield article
    }).collect { case Some(article) => article }
    articles
  }
}