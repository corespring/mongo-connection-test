
organization := "org.corespring"
name := "mongo-connection-test"

scalaVersion in ThisBuild := "2.10.5"

(javacOptions in Compile) ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.8.2",
  "org.mongodb" % "mongo-java-driver" % "2.11.3"
)
