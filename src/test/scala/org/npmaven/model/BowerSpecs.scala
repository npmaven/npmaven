package org.npmaven.model

import net.liftweb.http.LiftRules
import org.specs2.mutable.Specification

object BowerSpecs extends Specification {
  "Bower object" should {
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

  "Bower object for angular" should {
    val angular = Bower("angular", "1.3.14", "./angular.js", List())

    "extract the angular bower.json to a case class via String" in {
      val json = LiftRules.loadResourceAsString("/org/npmaven/model/angular-bower.json")
      val bower = json.flatMap(Bower(_))

      bower should be equalTo(Some(angular))
    }

    "extract the angular bower.json to a case class via bytes" in {
      val jsonBytes = LiftRules.loadResource("/org/npmaven/model/angular-bower.json")
      val bower = jsonBytes.flatMap(Bower(_))

      bower should be equalTo(Some(angular))
    }
  }

  "Bower object for d3" should {
    val d3 = Bower("d3", "3.5.5", "d3.js", List("d3.js"))

    "extract the d3 bower.json to a case class via String" in {
      val json = LiftRules.loadResourceAsString("/org/npmaven/model/d3-bower.json")
      val bower = json.flatMap(Bower(_))

      bower should be equalTo (Some(d3))
    }
  }
}

