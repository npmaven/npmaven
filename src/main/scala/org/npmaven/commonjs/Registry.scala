package org.npmaven
package commonjs

import model._
import dispatch._
import net.liftweb.common.Loggable

import scala.concurrent.ExecutionContext

class Registry(url:String)(implicit ec:ExecutionContext) extends Loggable {
  val host = dispatch.host(url)

  def get(pkg:Package):Future[Package] = {
    val req = (host / pkg.name / pkg.version)
      .setContentType("application/json", "UTF-8")
      .GET

    logger.trace("Getting "+req)

    Http(req OK as.String).flatMap { json =>
      logger.trace("Response => "+json)
      Package(json) match {
        case Some(p:Package) => Future.successful(p)
        case _ => Future.failed(new Exception("Service did not return a valid JSON object"))
      }
    }
  }
}
