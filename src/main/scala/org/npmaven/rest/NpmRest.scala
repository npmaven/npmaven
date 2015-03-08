package org.npmaven
package rest

import java.io.OutputStream

import model._
import commonjs. {Registry => NpmRegistry}
import util.FutureConversions._
import artifacts.Artifactory._

import net.liftweb.common.Loggable
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NpmRest extends RestHelper with Loggable {
  lazy val npm = new NpmRegistry("registry.npmjs.org/")

  def toResponse(pkg:Package, art:Artifact):Future[LiftResponse] = art match {
    case Pom => Future.successful(XmlResponse(pom(pkg)))
    case Jar => jar(pkg).map(b => OutputStreamResponse((out: OutputStream) => out write b))
    case Sha1(Pom) => {
      val sum = sha1(XmlResponse(pom(pkg)).toResponse.data)
      Future.successful(PlainTextResponse(sum, 200))
    }
    case _ => Future.successful(NotFoundResponse())
  }

  serve {
    case "repo" :: "npm" :: "org" :: "npmaven" :: name :: version :: artifact :: Nil RestReq _ => {
      val pkg = Package(name, version)
      val art = Artifact(artifact)
      logger.trace(S.request)

      val f = npm.get(pkg)
        .flatMap(p => art.map(a => toResponse(p, a)).openOr(Future.successful(NotFoundResponse())))
        .recover{case e:Exception => logger.trace(e); NotFoundResponse()}
        .la

      f
    }
  }

  private object RestReq {
    def unapply(r: Req): Option[(List[String], Req)] = Some(r.path.partPath -> r)
  }
}
