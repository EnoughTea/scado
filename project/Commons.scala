// This file contains standard settings for a project.
import sbt._
import Keys._

object Commons {
  val osName: String = System.getProperty("os.name").split(" ")(0).toLowerCase()
  val osPathSeparator: String = osName match { case "windows" => ";" case _ => ":" }

  val appVersion = "1.0"
  val appOrg = "com.enoughtea"
  val commonScalaVersion = "2.12.2"
  val scalaCompilerOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8",
    "-Yno-adapted-args",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code",
    "-Xfuture",
    "-Ywarn-unused"
    )

  val javaCompilerOptions = Seq(
    "-Xlint",
    "-encoding", "UTF-8",
    "-source", "1.8",
    "-target", "1.8"
  )

  val parallelTests = false

  // ScalaTest options:
  //  W - without color
  //  D - show all durations
  //  S - show short stack traces
  //  F - show full stack traces
  //  U - unformatted mode
  //  I - show reminder of failed and canceled tests without stack traces
  //  T - show reminder of failed and canceled tests with short stack traces
  //  G - show reminder of failed and canceled tests with full stack traces
  //  K - exclude TestCanceled events from reminder
  val testOpts = "-oD"
  // By default, a project exports a directory containing its resources and compiled class files.
  // Set useJarsOnClasspath to true to export the packaged jar instead.
  val useJarsOnClasspath = false

  val settings: Seq[Def.Setting[_]] = Seq(
    version := appVersion,
    organization := appOrg,
    scalaVersion := commonScalaVersion,
    scalacOptions ++= scalaCompilerOptions,
    javacOptions ++= javaCompilerOptions,

    parallelExecution in Test := parallelTests,
    testOptions += Tests.Argument(testOpts),

    exportJars := useJarsOnClasspath,
    cancelable in Global := true,

    resolvers += Opts.resolver.mavenLocalFile,
    resolvers += Resolver.typesafeRepo("releases"),
    resolvers += Resolver.jcenterRepo
  )
}