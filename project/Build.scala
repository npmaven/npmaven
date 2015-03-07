import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtSite.SiteKeys._

object RootBuild extends Build {
  val liftVersion = SettingKey[String]("liftVersion", "Full version number of the Lift Web Framework")
  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")  
  lazy val generateSiteTaskKey = TaskKey[Seq[File]]("generateSite", "Generates the web site from the sbt-site plugin")
  
  def listRecursively(f:File):Seq[String] = {
    def rec(f:File, prefix:String):Seq[String] = 
      if(f.isDirectory) f.listFiles().toSeq.flatMap(c => rec(c, prefix+f.getName+"/"))
      else Seq(prefix+f.getName)
    rec(f,"")
  }

  val generateSite = generateSiteTaskKey <<= (resourceManaged in Compile, siteDirectory) map { (rsrc: File, site: File) =>
    val relPaths = site.listFiles() flatMap listRecursively
    IO.copyDirectory(site, rsrc / "site")
    relPaths.map(name => new File(rsrc / "site", name))
  }

  lazy val project = Project(
    id = "npmaven", 
    base = file("."),
    settings = 
      Project.defaultSettings ++ Seq(
        liftEdition <<= liftVersion { _.substring(0,3) },
        generateSite,
        generateSiteTaskKey <<= generateSiteTaskKey dependsOn makeSite,
        compile in Compile <<= compile in Compile dependsOn generateSiteTaskKey
      )
  )
}
