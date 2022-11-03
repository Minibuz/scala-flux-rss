

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

    val res = Abonnement.retrieveById(0)(connection)
    //val res1 = Abonnement.retrieveByFlux("toto.com")(connection)
    println(res)
    //println(res1)
  }
}
