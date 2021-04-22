
lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """movie-recemmendation-play""",
    version := "2.6.x",
    scalaVersion := "2.12.13",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
    ),
    libraryDependencies ++= Seq(
      "org.apache.spark" % "spark-core_2.12" % "2.4.0",
      "org.apache.spark" % "spark-sql_2.12" % "2.4.0",
      "org.apache.spark" % "spark-streaming_2.12" % "2.4.0",
      "org.apache.spark" % "spark-mllib_2.12" % "2.4.0",
      "org.jmockit" % "jmockit" % "1.34" % "test"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )

  )
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.jiayouya.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.jiayouya.binders._"
