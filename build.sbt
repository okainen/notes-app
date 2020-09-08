name := """notes-app"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
    ehcache, ws, specs2 % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
    "org.scalamock" %% "scalamock" % "4.4.0" % Test,
    "com.softwaremill.macwire" %% "macros" % "2.3.7" % Provided,
    "org.webjars" % "swagger-ui" % "3.24.3",
    "org.postgresql" % "postgresql" % "42.2.13",
    "com.typesafe.slick" %% "slick" % "3.3.2",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
    "com.typesafe.play" %% "play-slick" % "5.0.0",
    "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
    "org.flywaydb" % "flyway-core" % "6.1.4",
    "org.flywaydb" %% "flyway-play" % "6.0.0",
    "com.typesafe.play" %% "play-mailer" % "8.0.0",
    "io.monix" %% "monix" % "3.2.2",
    "com.github.t3hnar" %% "scala-bcrypt" % "4.1",
    "io.jvm.uuid" %% "scala-uuid" % "0.3.1",
    "org.reactivemongo" %% "play2-reactivemongo" % "1.0.0-play27"
)

swaggerDomainNameSpaces := Seq("models")
