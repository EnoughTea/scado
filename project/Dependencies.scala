// This file contains all external dependencies.
import sbt._
import Keys._

object Dependencies {
  // Provides an automatic resource management to Scala (like using in C# and try-with-resources in Java).
  val scalaArmVersion = "2.0"
  val scalaArm = "com.jsuereth" %% "scala-arm" % scalaArmVersion

  // Scallop is the best command-line arguments parsing tool.
  val scallopVersion = "2.1.2"
  val scallop = "org.rogach" %% "scallop" % scallopVersion

  // Apache Commons is like a standard library :)
  val commonsIoVersion = "2.5"
  val commonsIo = "commons-io" % "commons-io" % commonsIoVersion

  val commonsLangVersion = "3.5"
  val commonsLang ="org.apache.commons" % "commons-lang3" % commonsLangVersion

  // Test frameworks:
  val scalatestVersion = "3.0.1"
  val scalacticVersion = scalatestVersion
  val scalactic = "org.scalactic" %% "scalactic" % scalacticVersion
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % "test"

  val commonDependencies: Seq[ModuleID] = Seq(
    scalaArm,
    scallop,
    commonsIo,
    commonsLang,
    scalactic,
    scalatest
  )
}