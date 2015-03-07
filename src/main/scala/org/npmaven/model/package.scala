package org.npmaven

import net.liftweb.common.{Failure, Full, Box}

package object model {
  case class Package(name:String, version:String)

  sealed trait Artifact
  case object Pom extends Artifact
  case object Jar extends Artifact
  case object Sources extends Artifact
  case object Docs extends Artifact
  object Artifact {
    def apply(filename:String):Box[Artifact] =
      if(filename endsWith ".pom") Full(Pom)
      else if(filename endsWith "sources.jar") Full(Sources)
      else if(filename endsWith "javadoc.jar") Full(Docs)
      else if(filename endsWith ".jar") Full(Jar)
      else Failure("Invalid artifact filename: "+filename)
  }
}
