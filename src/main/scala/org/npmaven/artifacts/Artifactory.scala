package org.npmaven
package artifacts

import java.security.MessageDigest

import model._

import scala.xml.Node

object Artifactory {
  def pom(pkg:Package):Node = {
    import pkg._

    <project>
      <modelVersion>4.0.0</modelVersion>
      <groupId>org.npmaven</groupId>
      <artifactId>{name}</artifactId>
      <version>{version}</version>
    </project>
  }

  def sha1(bytes:Array[Byte]):String = {
    val md = MessageDigest.getInstance("SHA-1")
    val digest = md digest bytes
    digest.map(b => String.format("%02x", b:java.lang.Byte)).mkString
  }
}
