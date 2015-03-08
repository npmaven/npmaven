package org.npmaven
package artifacts

import net.liftweb.common.Full
import net.liftweb.http.LiftRules
import org.specs2.mutable.Specification

object ArtifactorySha1Specs extends Specification {
  "Artifactory.sha1" should {
    "correctly calculate the sha1 of our canned angular-1.3.13.jar" in {
      val bytes = LiftRules.loadResource("/org/npmaven/artifacts/angular-1.3.13.jar")
      val sha1 = bytes.map(Artifactory.sha1)

      sha1 should be equalTo(Full("09de1e0dcf0b0873bcc58dffa5a6595121fb3cef"))
    }
  }
}
