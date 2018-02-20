package com.hnrklssn.sculpt.sbt

import sbt._
import sbt.Keys._

object SculptSbtPlugin extends AutoPlugin {
  val GroupId = "com.lightbend"
  val ArtifactId = "scala-sculpt"

  object autoImport {
    val Sculpt = config("sculpt") extend Compile

    lazy val sculpt = taskKey[Unit]("Run scala-sculpt dependency check")
    lazy val sculptVersion = settingKey[String]("The version of the scala plugin to use")
    lazy val sculptOutputPath = settingKey[String]("Directory where reports will be written")
  }
  import autoImport._

  override def trigger = allRequirements

  override def buildSettings = super.buildSettings ++ Seq(
    sculptVersion := "0.1.4-SNAPSHOT",
    //scalacOptions += "-Xplugin-require:sculpt"
  )

  override def projectSettings = {
    inConfig(Sculpt) {
      Defaults.compileSettings ++
        Seq(
          sources := (sources in Compile).value,
          managedClasspath := (managedClasspath in Compile).value,
          unmanagedClasspath := (unmanagedClasspath in Compile).value,
          scalacOptions := {
            // find all deps for the compile scope
            val sculptDependencies = (update in Sculpt).value matching configurationFilter(Provided.name)
            // ensure we have the sculpt dependency on the classpath and if so add it as a scalac plugin
            sculptDependencies.find(_.getAbsolutePath.contains(ArtifactId)) match {
              case None => throw new Exception(s"Fatal: $ArtifactId not in libraryDependencies ($sculptDependencies)")
              case Some(classpath) =>
                val depString = sculptDependencies.map(_.getAbsolutePath).mkString(java.io.File.pathSeparatorChar.toString)
                println(s"TESTESTAS: $depString")
                (scalacOptions in Compile).value ++ Seq(
                  Some("-Xplugin:" + depString),
                  Some("-Xplugin-require:sculpt"),
                  Some("-P:sculpt:out=" + sculptOutputPath.value)
                ).flatten
            }
          }
        )
    } ++ Seq(
      sculpt := (compile in Sculpt).value,
      // FIXME Cannot seem to make this a build setting (compile:crossTarget is an undefined setting)
      sculptOutputPath := {
        val outputFile = new File((crossTarget in Compile).value.getAbsolutePath + "\\sculpt-report")
        outputFile.mkdirs()
        outputFile.getAbsolutePath + "\\dep.json"
      },
      libraryDependencies ++= Seq(GroupId %% ArtifactId % (sculptVersion in ThisBuild).value % Provided))
  }
}
