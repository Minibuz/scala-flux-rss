import spark.Spark._
import spark.{Request, Response}

object main {
    val serverPort = 8090

    def main(args: Array[String]): Unit = {

      port(serverPort)

      get(
        "/hello",
        { (request: Request, response: Response) =>
          response.`type`("application/json")

          """{"message": "hello"}"""
        }
      )

      get(
        "/hello/:name",
        { (request: Request, response: Response) =>
          response.`type`("application/json")

          val name = request.params("name")

          s"""{"message": "hello $name"}"""
        }
      )

      get(
        "/message",
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