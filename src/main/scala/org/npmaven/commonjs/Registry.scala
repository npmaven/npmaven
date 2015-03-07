package org.npmaven
package commonjs

import model._
import dispatch._
import net.liftweb.json._
import scala.concurrent.ExecutionContext.Implicits.global

class Registry(url:String) {
  val host = dispatch.host(url)

  def get(pkg:Package):Future[JObject] = {
    val req = (host / pkg.name / pkg.version)
      .setContentType("application/json", "UTF-8")
      .GET

    println("Getting "+req)
    Http(req OK as.String).flatMap { json =>
      println("Reponse => "+json)
      parseOpt(json) match {
        case Some(obj:JObject) => Future.successful(obj)
        case _ => Future.failed(new Exception("Service did not return a valid JSON object"))
      }
    }
  }
}
