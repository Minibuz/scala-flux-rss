import Abonnement.Abonnement
import Cassandra.CassandraConnection
import User.User

import java.util.UUID
import scala.util.Random

object Injection {

  def injectDatas(connection : CassandraConnection): List[User] = {
    val listAbonnement: List[Abonnement] = List(
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

    List.range(1, 10).map(_ => {
      val elements = Random.between(1, 10)
      val shuffled = Random.shuffle(listAbonnement)
      User.createUser(shuffled.take(elements).map(abonnement => abonnement.idAbonnement.get))(connection)
    })
  }
}
