name := "sbt-scala-sculpt"

organization := "com.hnrklssn"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.3"

sbtPlugin := true

libraryDependencies ++= Seq()

lazy val scalaSculpt = RootProject(uri("https://github.com/lightbend/scala-sculpt.git#master"))

//libraryDependencies += Defaults.sbtPluginExtra("com.lightbend" %% "scala-sculpt" % "0.1.3", (sbtBinaryVersion in pluginCrossBuild).value, (scalaBinaryVersion in update).value)

lazy val root = (project in file(".")).dependsOn(scalaSculpt)