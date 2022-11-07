import Abonnement.Abonnement
import Cassandra.CassandraConnection
import User.User

import java.util.UUID
import scala.util.Random

object Injection {

  def injectDatas(connection : CassandraConnection): Unit = {
    val listUser : List[User] = List()
    var listAbonnement: List[Abonnement] = List(
      Abonnement.createAbonnement("https://www.lemonde.fr")(connection),
      Abonnement.createAbonnement("https://www.youtube.com")(connection),
      Abonnement.createAbonnement("https://ent.univ-eiffel.fr")(connection),
      Abonnement.createAbonnement("https://www.netflix.com")(connection),
      Abonnement.createAbonnement("https://mail.google.com")(connection),
      Abonnement.createAbonnement("https://www.impots.gouv.fr")(connection),
      Abonnement.createAbonnement("https://www.twitch.tv")(connection),
      Abonnement.createAbonnement("https://www.bfmtv.com")(connection),
      Abonnement.createAbonnement("https://www.disneyplus.com")(connection),
      Abonnement.createAbonnement("https://www.primevideo.com")(connection),
      Abonnement.createAbonnement("https://www.root-me.org")(connection)
    )
    for(i <- 1 to 10) {
      val size : Int = Random.between(1, 10)
      User.createUser(List(UUID.fromString(listAbonnement.take(size).foreach[UUID](Abonnement => Abonnement.idAbonnement.get).toString)))(connection) :: listUser
      listAbonnement = Random.shuffle(listAbonnement)
    }
  }
}
