

object MainAbonnement {
  val serverPort = 8090
  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Abonnement.createTable(connection)
    val abonnement = new Abonnement.Abonnement(0, "test.com")
    abonnement.insert(connection)
    val abonnement1 = new Abonnement.Abonnement(1, "toto.com")
    abonnement.insert(connection)

    val abonnement = Abonnement.createAbonnement("test.com")(connection)
    val abonnement1 = Abonnement.createAbonnement("toto.com")(connection)
    println(abonnement.idAbonnement.get)
    val res = Abonnement.retrieveById(abonnement.idAbonnement.get)(connection)
    val res1 = Abonnement.retrieveByFlux("test.com")(connection)

    println(res)
    println(res1)
  }
}
