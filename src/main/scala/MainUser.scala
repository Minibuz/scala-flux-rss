import java.util

object MainUser {
  val serverPort = 8090

  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Abonnement.Data.createTableById(connection)
    val abonnement = new Abonnement.Abonnement(0, "test.com")
    abonnement.insert(connection)
    val abonnement1 = new Abonnement.Abonnement(1, "toto.com")
    abonnement.insert(connection)
    val abonnement2 = new Abonnement.Abonnement(2, "tata.com")
    abonnement.insert(connection)
    abonnement1.insert(connection)
    abonnement2.insert(connection)


    User.createTableById(connection)
    val list : List[Long] = List(0,1,2)
    val user = new User.User(0, list)
    user.insert(connection)
    val res = User.retrieveById(0)(connection)

    //val resA = Abonnement.Data.retrieveById(1)(connection)
    val res2 = User.retrieveListAbonnementById(0)(connection)
    println(res2)
  }
}
