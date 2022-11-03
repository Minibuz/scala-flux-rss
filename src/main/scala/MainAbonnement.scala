

object MainAbonnement {
  val serverPort = 8090
  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Abonnement.createTableById(connection)

    val abonnement = Abonnement.createAbonnement("test.com")(connection)
    val abonnement1 = Abonnement.createAbonnement("toto.com")(connection)
    println(abonnement.idAbonnement.get)
    val res = Abonnement.retrieveById(abonnement.idAbonnement.get)(connection)
    val res1 = Abonnement.retrieveByFlux("test.com")(connection)

    println(res)
    println(res1)
  }
}
