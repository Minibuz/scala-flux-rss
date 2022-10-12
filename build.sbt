ThisBuild / scalaVersion := "2.13.8"

val libVersion =
  new {
    val cassandra      = "4.0.6"
    val javaSpark      = "2.9.4"
    val kafka          = "3.3.1"
    val scalatest      = "3.2.14"
    val testcontainers = "1.17.5"
  }

lazy val root = (project in file("."))
  .settings(
    name := "scala_flux_rss"
  )

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.sparkjava"  % "spark-core" % libVersion.javaSpark,
  "org.apache.kafka" % "kafka-clients" % libVersion.kafka,
  "org.testcontainers" % "testcontainers" % libVersion.testcontainers,
  "org.apache.cassandra" % "cassandra-all" % libVersion.cassandra,
  "org.scalatest" %% "scalatest" % libVersion.scalatest % Test
)

Global / onChangedBuildSource := ReloadOnSourceChanges