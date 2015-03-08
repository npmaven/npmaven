package org.npmaven
package artifacts

import model._

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

object ArtifactorySpecs extends Specification with XmlMatchers {
  "Artifactory object" should {
    "produce a minimal pom" in {
      val pkg = Package("angular", "1.3.14")
      val pom =
        <project>
          <modelVersion>4.0.0</modelVersion>
          <groupId>org.npmaven</groupId>
          <artifactId>angular</artifactId>
          <version>1.3.14</version>
        </project>

      Artifactory.pom(pkg) must ==/ (pom)
    }
  }
}
