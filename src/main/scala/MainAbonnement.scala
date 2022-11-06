import Abonnement.Abonnement

object MainAbonnement {
  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")


    val abonnement = Abonnement.createAbonnement("test.com")(connection)
    abonnement.insert(connection)
    val abonnement2 = Abonnement.createAbonnement("toto.com")(connection)
    abonnement2.insert(connection)

    println(abonnement.idAbonnement.get)
    val res = Abonnement.retrieveById(abonnement.idAbonnement.get)(connection)
    val res1 = Abonnement.retrieveByFlux("test.com")(connection)

    println(res.getOrElse(""))
    println(res1.getOrElse(""))
  }
}
