// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `jwt-akka-http` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GitVersioning, GitBranchPrompt)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.akkaHttpCirce,
        library.authenticatJwt,
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
      val akkaHttp       = "10.0.9"
      val akkaHttpCirce  = "1.17.0"
      val authenticatJwt = "0.4.5"
      val circeGeneric   = "0.8.0"
      val scalaCheck     = "1.13.5"
      val scalaTest      = "3.0.3"
    }
    val akkaHttp       = "com.typesafe.akka" %% "akka-http"           % Version.akkaHttp
    val akkaHttpCirce  = "de.heikoseeberger" %% "akka-http-circe"     % Version.akkaHttpCirce
    val authenticatJwt = "com.jason-goodwin" %% "authentikat-jwt"     % Version.authenticatJwt
    val circeGeneric   = "io.circe"          %% "circe-generic"       % Version.circeGeneric
    val scalaCheck     = "org.scalacheck"    %% "scalacheck"          % Version.scalaCheck
    val scalaTest      = "org.scalatest"     %% "scalatest"           % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  gitSettings

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

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )
