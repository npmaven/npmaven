package org.npmaven
package rest

import model._
import commonjs. {Registry => NpmRegistry}
import util.FutureConversions._
import artifacts.Artifactory._

import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import scala.concurrent.ExecutionContext.Implicits.global

object NpmRest extends RestHelper with Loggable {
  lazy val npm = new NpmRegistry("registry.npmjs.org/")

  def toResponse(pkg:Package, art:Artifact):LiftResponse = art match {
    case Pom => XmlResponse(pom(pkg))
    case Sha1(Pom) => {
      val sum = sha1(XmlResponse(pom(pkg)).toResponse.data)
      PlainTextResponse(sum, 200)
    }
    case _ => NotFoundResponse()
  }

  serve {
    case "repo" :: "npm" :: "org" :: "npmaven" :: name :: version :: artifact :: Nil XmlReq _ => {
      val pkg = Package(name, version)
      val art = Artifact(artifact)
      logger.trace(S.request)

      val f = npm.get(pkg)
        .map(p => art.map(a => toResponse(p, a)).openOr(NotFoundResponse()))
        .recover{case e:Exception => logger.trace(e); NotFoundResponse()}
        .la

      f
    }
  }
}
