package org.npmaven
package artifacts

import model._

import scala.xml.NodeSeq

object Artifactory {
  def pom(pkg:Package):NodeSeq = {
    import pkg._

    <project>
      <modelVersion>4.0.0</modelVersion>
      <groupId>org.npmaven</groupId>
      <artifactId>{name}</artifactId>
      <version>{version}</version>
    </project>
  }
}
