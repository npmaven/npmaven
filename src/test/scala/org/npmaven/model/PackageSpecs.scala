package org.npmaven
package model

import java.util.Properties

import net.liftweb.http.LiftRules
import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

object PackageSpecs extends Specification with XmlMatchers {
  "Package object" should {
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
  }

  "Package object for angular" should {
    val angular = Package(
      "angular", "1.3.13",
      main = Some("angular.js"),
      license = Some("MIT"),
      dist = Some(Distribution(
        "f00586f575c9be970847b449c4657f23f76cf8a5",
        "http://registry.npmjs.org/angular/-/angular-1.3.13.tgz"
      )))

    "extract the json to a case class" in {
      val json = LiftRules.loadResourceAsString("/org/npmaven/model/angular.json")
      val pkg = json.flatMap(Package(_))

      pkg should be equalTo(Some(angular))
    }

    "write the properties" in {
      val props = new Properties()
      props.setProperty("name", "angular")
      props.setProperty("version", "1.3.13")
      props.setProperty("main", "angular.js")
      props.setProperty("bower.main", "./angular.js")
      props.setProperty("license", "MIT")

      val pkg = angular.copy(bowerMain = Some("./angular.js"))

      pkg.asProperties should be equalTo(props)
    }
  }

  "Package object for d3" should {
    val d3 = Package(
      "d3", "3.5.5",
      main = Some("index.js"),
      license = None // TODO
    )

    "extract the json to a case class" in {
      val json = LiftRules.loadResourceAsString("/org/npmaven/model/d3.json")
      val pkg = json.flatMap(Package(_))

      pkg should be equalTo(Some(d3))
    }

    "write the properties" in {
      val props = new Properties()
      props.setProperty("name", "d3")
      props.setProperty("version", "3.5.5")
      props.setProperty("main", "index.js")
      props.setProperty("bower.main", "d3.js")
      props.setProperty("bower.scripts.0", "d3.js")
//      props.setProperty("license", "MIT")

      val pkg = d3
        .copy(bowerMain = Some("d3.js"))
        .copy(bowerScripts = List("d3.js"))

      pkg.asProperties should be equalTo(props)
    }
  }
}
