import sbt.Keys.scalaVersion

// Supported versions
val scala213 = "2.13.13"
val scala32 = "3.2.2"

ThisBuild / description := "Generic WebServices library currently only with Play WS impl./backend"

ThisBuild / organization := "io.cequence"
ThisBuild / scalaVersion := scala213
ThisBuild / version := "0.6.2-pekko"
ThisBuild / isSnapshot := false
ThisBuild / crossScalaVersions := List(scala213, scala32)

// POM settings for Sonatype
ThisBuild / homepage := Some(
  url("https://github.com/cequence-io/ws-client")
)

ThisBuild / sonatypeProfileName := "io.cequence"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/cequence-io/ws-client"),
    "scm:git@github.com:cequence-io/ws-client.git"
  )
)

ThisBuild / developers := List(
  Developer(
    "bburdiliak",
    "Boris Burdiliak",
    "boris.burdiliak@cequence.io",
    url("https://cequence.io")
  ),
  Developer(
    "bnd",
    "Peter Banda",
    "peter.banda@protonmail.com",
    url("https://peterbanda.net")
  )
)

ThisBuild / licenses += "MIT" -> url("https://opensource.org/licenses/MIT")
ThisBuild / publishMavenStyle := true
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
ThisBuild / publishTo := sonatypePublishToBundle.value

inThisBuild(
  List(
    scalacOptions += "-Ywarn-unused",
    //    scalaVersion := "2.12.15",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

// JSON
lazy val playJsonVersion = settingKey[String]("Play JSON version to use")

inThisBuild(
  playJsonVersion := {
    scalaVersion.value match {
      case "2.13.11" => "2.10.0-RC7"
      case "3.2.2"   => "2.10.0-RC6"
      case _         => "2.8.2"
    }
  }
)

// Pekko
lazy val pekkoStreamLibs = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) =>
      Seq(
        "org.apache.pekko" %% "pekko-stream" % "1.1.2"
      )
    case Some((3, _)) =>
      Seq(
        "org.apache.pekko" %% "pekko-stream" % "1.1.2"
      )
    case _ =>
      throw new Exception("Unsupported scala version")
  }
}

val loggingLibs = Def.setting {
  Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "ch.qos.logback" % "logback-classic" % "1.4.14" // requires JDK11, in order to use JDK8 switch to 1.3.5
  )
}

val pekkoHttpVersion = "1.1.0"

// Play WS
def orgPlayWS(version: String) = Seq(
  "org.playframework" %% "play-ahc-ws-standalone" % version,
  "org.playframework" %% "play-ws-standalone-json" % version
)

lazy val playWsDependencies = Def.setting {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) =>
      orgPlayWS("3.0.6")

    case Some((3, 2)) =>
      orgPlayWS("3.0.6")

    case Some((3, 3)) =>
      orgPlayWS("3.0.6")

    // failover to the latest version
    case _ =>
      orgPlayWS("3.0.6")
  }
}

lazy val `ws-client-core` =
  (project in file("ws-client-core")).settings(
    name := "ws-client-core",
    libraryDependencies ++= pekkoStreamLibs.value,
    libraryDependencies += "com.typesafe.play" %% "play-json" % playJsonVersion.value,
    publish / skip := false
  )

lazy val `ws-client-play` =
  (project in file("ws-client-play"))
    .settings(
      name := "ws-client-play",
      libraryDependencies ++= playWsDependencies.value,
      publish / skip := false
    )
    .dependsOn(`ws-client-core`)
    .aggregate(`ws-client-core`)

lazy val `ws-client-play-stream` =
  (project in file("ws-client-play-stream"))
    .settings(
      name := "ws-client-play-stream",
      libraryDependencies += "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion, // JSON WS Streaming
      libraryDependencies ++= loggingLibs.value,
      publish / skip := false
    )
    .dependsOn(`ws-client-core`, `ws-client-play`)
    .aggregate(`ws-client-core`, `ws-client-play`)
