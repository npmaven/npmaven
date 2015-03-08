package org.npmaven
package rest

import model.{Artifact, Package}
import commonjs. {Registry => NpmRegistry}
import util.FutureConversions._

import net.liftweb.common.Loggable
import net.liftweb.http.{XmlResponse, S, NotFoundResponse}
import net.liftweb.http.rest.RestHelper
import scala.concurrent.ExecutionContext.Implicits.global

object NpmRest extends RestHelper with Loggable {
  lazy val npm = new NpmRegistry("registry.npmjs.org/"
  )
  serve {
    case "repo" :: "npm" :: "org" :: "npmaven" :: name :: version :: artifact :: Nil XmlReq _ => {
      val pkg = Package(name, version)
      val art = Artifact(artifact)
      logger.trace(S.request)

      val f = npm.get(pkg)
        .map(_ => XmlResponse(<project></project>))
        .recover{case e:Exception => logger.trace(e); NotFoundResponse()}
        .la

      f
    }
  }
}
