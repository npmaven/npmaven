package org.npmaven
package rest

import org.npmaven.model.{Artifact, Package}

import net.liftweb.common.{Full, Loggable}
import net.liftweb.http.{LiftResponse, XmlResponse, S, NotFoundResponse}
import net.liftweb.http.rest.RestHelper

object NpmRest extends RestHelper with Loggable {
  serve {
    case "repo" :: "npm" :: "org" :: "npmaven" :: name :: version :: artifact :: Nil XmlReq _ => {
      val pkg = Package(name, version)
      val art = Artifact(artifact)
      println(S.request)

      val res:LiftResponse = if(name == "angular") XmlResponse(<project></project>)
      else NotFoundResponse()

      Full(res)
    }
  }
}
