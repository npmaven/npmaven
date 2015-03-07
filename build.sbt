import NativePackagerKeys._

name := "npmaven"

version := "0.0.1"

organization := "org.npmaven"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "https://oss.sonatype.org/content/repositories/releases",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases" // For specs2 3.0
)

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

scalacOptions in Test ++= Seq("-Yrangepos") // Recommended for specs2 3.0

liftVersion <<= liftVersion ?? "3.0-M3"

libraryDependencies ++= {
  val lv = liftVersion.value
  val le = liftEdition.value
  Seq(
    "net.liftweb"             %% "lift-webkit"              % liftVersion.value     % "compile",
    "net.databinder.dispatch" %% "dispatch-core"            % "0.11.2"              % "compile",
    "org.eclipse.jetty"       % "jetty-webapp"              % "9.2.7.v20150116"     % "compile",
    "org.eclipse.jetty"       % "jetty-plus"                % "9.2.7.v20150116"     % "container,test", // For Jetty Config
    "org.eclipse.jetty.orbit" % "javax.servlet"             % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          % "logback-classic"           % "1.0.6"               % "runtime",
    "org.specs2"              %% "specs2-core"              % "3.0"                 % "test",
    "org.specs2"              %% "specs2-matcher-extra"     % "3.0"                 % "test"   // For XmlMatchers
  )
}

packageArchetype.java_application

// Drops all of the webapp stuff in the /webapp on the classpath for the jetty WebAppContext to find
resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map { (managedBase, base) =>
  val webappBase = base / "src" / "main" / "webapp"
  for {
    (from, to) <- webappBase ** "*" pair rebase(webappBase, managedBase / "main" / "webapp")
  } yield {
    Sync.copy(from, to)
    to
  }
}

bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")

// So we can build src/pamflet
site.settings

site.pamfletSupport()

(webappResources in Compile) <+= (resourceManaged in Compile) { _ / "webapp" }
