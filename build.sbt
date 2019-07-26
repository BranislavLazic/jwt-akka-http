// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `jwt-akka-http` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.akkaHttpCirce,
        library.authentikatJwt,
        library.circeGeneric,
        library.scalaCheck % Test,
        library.scalaTest  % Test
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akkaHttp       = "10.1.9"
      val akkaHttpCirce  = "1.27.0"
      val authentikatJwt = "0.4.5"
      val circeGeneric   = "0.11.1"
      val scalaCheck     = "1.14.0"
      val scalaTest      = "3.0.8"
    }
    val akkaHttp       = "com.typesafe.akka" %% "akka-http"           % Version.akkaHttp
    val akkaHttpCirce  = "de.heikoseeberger" %% "akka-http-circe"     % Version.akkaHttpCirce
    val authentikatJwt = "com.jason-goodwin" %% "authentikat-jwt"     % Version.authentikatJwt
    val circeGeneric   = "io.circe"          %% "circe-generic"       % Version.circeGeneric
    val scalaCheck     = "org.scalacheck"    %% "scalacheck"          % Version.scalaCheck
    val scalaTest      = "org.scalatest"     %% "scalatest"           % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  scalafmtSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.2",
    organization := "ba.codecentric",
    organizationName := "Branislav Lazic",
    startYear := Some(2017),
    licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8"
    ),
    unmanagedSourceDirectories.in(Compile) := Seq(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Seq(scalaSource.in(Test).value)
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
  )