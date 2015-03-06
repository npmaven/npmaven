name := "npmaven"

version := "0.0.1"

organization := "org.npmaven"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases"        at "https://oss.sonatype.org/content/repositories/releases"
)

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

liftVersion <<= liftVersion ?? "3.0-M3"

libraryDependencies ++= {
  val lv = liftVersion.value
  val le = liftEdition.value
  Seq(
    "net.liftweb"             %% "lift-webkit"              % liftVersion.value     % "compile",
    "net.liftmodules"         %% ("lift-jquery-module_"+le) % "2.9-SNAPSHOT"        % "compile", // https://github.com/karma4u101/lift-jquery-module
    "org.eclipse.jetty"       % "jetty-webapp"              % "9.2.7.v20150116"     % "compile",
    "org.eclipse.jetty"       % "jetty-plus"                % "9.2.7.v20150116"     % "container,test", // For Jetty Config
    "org.eclipse.jetty.orbit" % "javax.servlet"             % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"          % "logback-classic"           % "1.0.6"               % "runtime",
    "org.specs2"              %% "specs2"                   % "2.3.12"              % "test"
  )
}

// Borrowed from https://github.com/vn971/roboCup
assemblyJarName := "npmaven.jar"

packageDescription <+= description

packageSummary <+= description

serverLoading in Debian := com.typesafe.sbt.packager.archetypes.ServerLoader.SystemV

bashScriptExtraDefines += "addJava '-Drun.mode=production'" // for liftweb

enablePlugins(JavaServerAppPackaging)

resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map { (managedBase, base) =>
	val webappBase = base / "src" / "main" / "webapp"
	for {
		(from, to) <- webappBase ** "*" pair rebase(webappBase, managedBase / "main" / "webapp")
	} yield {
		Sync.copy(from, to)
		to
	}
}


stage <<= stage dependsOn assembly