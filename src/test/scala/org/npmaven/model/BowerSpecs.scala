package org.npmaven.model

import net.liftweb.http.LiftRules
import org.specs2.mutable.Specification

object BowerSpecs extends Specification {
  "Bower object" should {
    val angular = Bower("angular", "1.3.14", "./angular.js")

    "extract the angular.json to a case class via String" in {
      val json = LiftRules.loadResourceAsString("/org/npmaven/model/bower.json")
      val bower = json.flatMap(Bower(_))

      bower should be equalTo(Some(angular))
    }

    "extract the angular.json to a case class via bytes" in {
      val jsonBytes = LiftRules.loadResource("/org/npmaven/model/bower.json")
      val bower = jsonBytes.flatMap(Bower(_))

      bower should be equalTo(Some(angular))
    }

    "extract a None if missing the main" in {
      val json = """{
        "name":"yo-momma",
        "version":"42"
      }"""

      Bower(json) should be equalTo(None)
    }

    "extract a None if the json is just garbage" in {
      val json = "garbage"

      Bower(json) should be equalTo(None)
    }
  }
}
