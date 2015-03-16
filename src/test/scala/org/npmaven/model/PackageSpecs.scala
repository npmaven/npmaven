package org.npmaven
package model

import net.liftweb.http.LiftRules
import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

object PackageSpecs extends Specification with XmlMatchers {
  "Package object" should {
    val angular = Package(
      "angular", "1.3.13",
      Some("angular.js"),
      Some("MIT"),
      Some(Distribution(
        "f00586f575c9be970847b449c4657f23f76cf8a5",
        "http://registry.npmjs.org/angular/-/angular-1.3.13.tgz"
      )))

    "extract the angular.json to a case class" in {
      val json = LiftRules.loadResourceAsString("/org/npmaven/model/angular.json")
      val pkg = json.flatMap(Package(_))

      pkg should be equalTo(Some(angular))
    }

    "extract a case class with only name/version" in {
      val json = """{
        "name":"d3",
        "version":"42"
      }"""
      val pkg = Package(json)
      val goal = Some(Package("d3", "42"))

      pkg should be equalTo(goal)
    }

    "extract a None if missing the name" in {
      val json = """{
        "version":"42"
      }"""

      Package(json) should be equalTo(None)
    }

    "extract a None if the json is just garbage" in {
      val json = "garbage"

      Package(json) should be equalTo(None)
    }

    "write to properties" in {
      val props = Map(
        "name" -> "angular",
        "version" -> "1.3.13",
        "main" -> "angular.js",
        "license" -> "MIT"
      )

      angular.asProperties should be equalTo(props)
    }
  }
}
