import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.{CqlIdentifier, CqlSession}
import com.datastax.oss.driver.api.core.cql.{PrepareRequest, ResultSet, Row, SimpleStatement}
import com.datastax.oss.driver.api.querybuilder.QueryBuilder._
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert
import com.datastax.oss.driver.api.querybuilder.relation.Relation._
import com.datastax.oss.driver.api.querybuilder.select.Select

import scala.io.Source
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Success, Try, Using}
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

object DocumentOp {

  case class Document(
                        id: Int,
                        message: String
                      ) {

  /**
   * We provide this function to insert an instance of Temperature
   * inside Cassandra. Because ''state'' is Optional, we have to
   * handle both cases when there is a state or not.
   *
   * By default, every column in Cassandra is optional, so we don't
   * have to insert a state when the state is None.
   */
  def insert(tableName: String)(session: CqlSession): Unit = {
    val baseQuery: RegularInsert =
      insertInto(tableName)
        .value("id", literal(id))
        .value("message", literal(message))

    val statement: SimpleStatement = baseQuery.build
    session.execute(statement)
  }
}

object Document {

  val DOCUMENT = "DOCUMENT"

  /**
   * This is a helper function to convert a Cassandra row into a
   * Temperature. Because a row can be anything, we have to handle the
   * case when we have a row that doesn't correspond to a temperature,
   * thus returning an '''Try[Temperature]'''.
   */
  def fromCassandra(row: Row): Try[Document] =
    Try(
      Document(
        id = 1,
        message = "Test"
      )
    )

  def createTableById(session: CqlSession): Unit = {
    val query =
      SchemaBuilder
        .createTable(DOCUMENT)
        .ifNotExists()
        .withPartitionKey("id", DataTypes.INT)
        .withColumn("message", DataTypes.TEXT)

    val statement: SimpleStatement = query.build
    session.execute(statement)
  }

  private def retrieve(query: Select)(session: CqlSession): List[Document] = {
    val statement = query.build
    val result: ResultSet = session.execute(statement)

    result.all().asScala.toList.map(fromCassandra).collect { case Success(v) => v }
  }

  /**
   * In our first query, we decided to use '''year''' as our partition
   * key, allowing us to extract quickly all the temperatures for a
   * particular year.
   */
  def retrieveById(id: Int)(session: CqlSession): List[Document] = {
    val query =
      selectFrom(DOCUMENT)
        .all()
        .where(column("id").isEqualTo(literal(id)))
    retrieve(query)(session)
  }
}

def insertDocument(tableName: String)(session: CqlSession): Unit = {
  val inputStream = new GZIPInputStream(new FileInputStream("data/climate/city_temperature.csv.gz"))

  Using(Source.fromInputStream(inputStream)) { file =>
    for (line <- file.getLines().toSeq.tail.take(5)) {
      val columns = line.split(",")
      val document =
        Document(
          id = columns(0).toInt,
          message = columns(1)
        )
      document.insert(tableName)(session)
    }
    }
  }
}