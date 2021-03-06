name := "Scalagram"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.github.pengrad" % "java-telegram-bot-api" % "1.3.2",
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",
  "com.typesafe.akka" % "akka-http-experimental_2.11" % "2.4.2",
  "com.google.code.gson" % "gson" % "2.6.2",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.2",
  "commons-io" % "commons-io" % "2.4"
)