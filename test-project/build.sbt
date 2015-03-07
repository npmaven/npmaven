name := "npmaven-test"

version := "0.0.1"

organization := "org.npmaven"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "https://oss.sonatype.org/content/repositories/releases",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases", // For specs2 3.0
  "localhost" at "http://localhost:8080/repo/npm" 
)

scalacOptions ++= Seq("-deprecation", "-unchecked")

scalacOptions in Test ++= Seq("-Yrangepos") // Recommended for specs2 3.0

libraryDependencies ++= {
  Seq(
    "org.npmaven"             %  "angular"                  % "1.3.14"              % "compile",
    "org.specs2"              %% "specs2-core"              % "3.0"                 % "test",
    "org.specs2"              %% "specs2-matcher-extra"     % "3.0"                 % "test"   // For XmlMatchers
  )
}
