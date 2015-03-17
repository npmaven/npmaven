package org.npmaven

import java.util.Properties

import net.liftweb.common.{Empty, Failure, Full, Box}

package object model {
  case class Distribution(shasum:String, tarball:String)
  case class Package(
    name:String,
    version:String,
    main:Option[String] = None,
    license:Option[String] = None,
    dist:Option[Distribution] = None,
    mainBower:Option[String] = None
  ) {
    val asProperties:Properties = {
      val properties = new Properties()
      List(
        Some(("name", name)),
        Some(("version", version)),
        main.map(("main.npm", _)),
        mainBower.map(("main.bower", _)),
        license.map(("license", _))
      ).flatten.foreach{ case (k, v) =>
        properties.setProperty(k, v)
      }
      properties
    }
  }

  object Package {
    import net.liftweb.json
    import net.liftweb.json._

    implicit val formats = json.DefaultFormats
    def apply(string:String):Option[Package] = parseOpt(string).flatMap { json =>
      json match {
        case obj:JObject => apply(obj)
        case _ => None
      }
    }
    def apply(json:JObject):Option[Package] = json.extractOpt[Package]
  }

  case class Bower(
    name:String,
    version:String,
    main:String
  )

  object Bower {
    import net.liftweb.json
    import net.liftweb.json._

    implicit val formats = json.DefaultFormats
    def apply(string:String):Option[Bower] = parseOpt(string).flatMap { json =>
      json match {
        case obj:JObject => apply(obj)
        case _ => None
      }
    }
    def apply(json:JObject):Option[Bower] = json.extractOpt[Bower]
    def apply(bytes:Array[Byte]):Option[Bower] = apply(new String(bytes, "iso-8859-1"))
  }

  sealed trait Artifact
  case object Pom extends Artifact
  case object Jar extends Artifact
  case object Sources extends Artifact
  case object Docs extends Artifact
  case class Sha1(of:Artifact) extends Artifact
  object Artifact {
    def apply(filename:String):Box[Artifact] =
      if(filename endsWith ".pom") Full(Pom)
      else if(filename endsWith ".pom.sha1") Full(Sha1(of = Pom))
      else if(filename endsWith "sources.jar") Full(Sources)
      else if(filename endsWith "javadoc.jar") Full(Docs)
      else if(filename endsWith ".jar") Full(Jar)
      else Failure("Invalid artifact filename: "+filename)
  }
}
