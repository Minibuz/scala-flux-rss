import spark.Spark._
import spark.{Request, Response}

object main {
    val serverPort = 8090

    def main(args: Array[String]): Unit = {

      port(serverPort)

      get(
        "/articles", "application/json",
        { (request: Request, response: Response) =>
          println(request.queryParams("user_id"))
          response.`type`("application/json")

          """{"message": "hello"}"""
        }
      )

      get(
        "/hello/:article_id",
        { (request: Request, response: Response) =>
          response.`type`("application/json")

          val name = request.params("article_id")

          s"""{"message": "hello $name"}"""
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
}