package org.npmaven
package commonjs

import model._
import dispatch._
import net.liftweb.common.Loggable
import net.liftweb.json._
import scala.concurrent.ExecutionContext.Implicits.global

class Registry(url:String) extends Loggable {
  val host = dispatch.host(url)

  def get(pkg:Package):Future[JObject] = {
    val req = (host / pkg.name / pkg.version)
      .setContentType("application/json", "UTF-8")
      .GET

    logger.trace("Getting "+req)

    Http(req OK as.String).flatMap { json =>
      logger.trace("Reponse => "+json)
      parseOpt(json) match {
        case Some(obj:JObject) => Future.successful(obj)
        case _ => Future.failed(new Exception("Service did not return a valid JSON object"))
      }
    }
  }
}
