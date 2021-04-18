name := "movie-recommandation-systems"
version := "1.0"
scalaVersion := "2.11.12"
libraryDependencies ++= Seq(
"org.apache.spark" % "spark-core_2.11" % "2.4.0",
"org.apache.spark" % "spark-sql_2.11" % "2.4.0",
"org.apache.spark" % "spark-streaming_2.11" % "2.4.0",
"org.apache.spark" % "spark-mllib_2.11" % "2.4.0",
"org.jmockit" % "jmockit" % "1.34" % "test"
)
libraryDependencies++=Seq(
  "org.apache.spark"%"spark-core_2.10"%"1.6.0",
  "org.apache.spark"%"spark-sql_2.10"%"1.6.0"
)