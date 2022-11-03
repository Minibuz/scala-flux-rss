import java.util
import java.util.UUID

object MainUser {
  val serverPort = 8090

  def main(args: Array[String]): Unit = {
    val connection = Cassandra.connect()

    connection.createKeyspace("my_keyspace")
    connection.useKeyspace("my_keyspace")

    Abonnement.createTableById(connection)
    val abonnement = Abonnement.createAbonnement("guillaume.com")(connection)
    val abonnement1 =  Abonnement.createAbonnement("robin.com")(connection)
    val abonnement2 =  Abonnement.createAbonnement( "leo.com")(connection)
    User.createTableById(connection)
    val list : List[UUID] = List(abonnement.idAbonnement.get, abonnement1.idAbonnement.get, abonnement2.idAbonnement.get)
    //println(list)
    val user = User.createUser(list)(connection)
    println(user.idUser.get)
    val res = User.retrieveById(user.idUser.get)(connection)
    println(res)
    val res2 = User.retrieveListAbonnementByUser(user)(connection)
    println(res2)
  }
}
