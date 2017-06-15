// This file contains project definitions for SBT.
import Dependencies._

lazy val scado = (project in file(".")).
  settings(Commons.settings: _*).
  settings(
    libraryDependencies ++= commonDependencies,
    mainClass in assembly := Some("scado.ConsoleApp"),
    assemblyOutputPath in assembly := file("build/scado.jar")
  )