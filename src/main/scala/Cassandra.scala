import com.datastax.oss.driver.api.core.cql.{ResultSet, SimpleStatement}
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder

import scala.util.Try

object Cassandra {

  case class CassandraConnection(
                                cqlSession: CqlSession
                                ) {

    def createKeyspace(keyspaceName: String): Unit = {
      val createKeyspaceQuery =
        SchemaBuilder
          .createKeyspace(keyspaceName)
          .ifNotExists
          .withSimpleStrategy(1)

      cqlSession.execute(createKeyspaceQuery.build)
    }

    def useKeyspace(keyspaceName: String): Unit = {
      val useKeyspaceQuery =
        "USE " + CqlIdentifier.fromCql(keyspaceName)

      cqlSession.execute(useKeyspaceQuery)
    }
    def getArticleId: Long = {
      val query =
        selectFrom("ARTICLE")
          .columns("articleId")
          .limit(1)
      val statement = query.build()
      val result : ResultSet = cqlSession.execute(statement)
      result.all().size()
    }

    def execute(statement: SimpleStatement): ResultSet = {
      cqlSession.execute(statement)
    }
  }

  def connect(): CassandraConnection = {
    val session = CqlSession
      .builder
      .build()

    CassandraConnection(session)
  }
}
