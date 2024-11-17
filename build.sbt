//ThisBuild / version := "0.1.0-SNAPSHOT"
//
//ThisBuild / scalaVersion := "2.13.8"
//
//lazy val root = (project in file("."))
//  .settings(
//    name := "WordCountSBT"
//  )
name := "WordCountSBT"

version := "1.0"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0"
)

enablePlugins(AssemblyPlugin)

assembly / assemblyMergeStrategy := {
  case PathList("example", xs @ _*) => MergeStrategy.first
  case x => MergeStrategy.defaultMergeStrategy(x)
}
