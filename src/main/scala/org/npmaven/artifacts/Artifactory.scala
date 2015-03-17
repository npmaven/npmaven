package org.npmaven
package artifacts

import java.io._
import java.util.Properties

import model._

import java.security.MessageDigest
import java.util.jar._
import java.util.zip._

import net.liftweb.common.Loggable
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.utils.IOUtils

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Node

import dispatch._

object Artifactory extends Loggable {
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

  def jar(pkg:Package)(implicit ec:ExecutionContext):Future[Array[Byte]] = {
    pkg.dist.map { dist =>
      val req = url(dist.tarball).GET
      Http(req OK as.Bytes).map(b => pkgToJar(pkg, b))
    }.getOrElse(Future.failed(new Exception("Package did not contain a dist.tarball value")))
  }

  private def manifest(pkg:Package):ListMap[String, String] =
    ListMap(
      "Manifest-Version" -> "1.0",
      "Implementation-Vendor" -> "org.npmaven",
      "Implementation-Title" -> pkg.name,
      "Implementation-Version" -> pkg.version,
      "Implementation-Vendor-Id" -> "org.npmaven",
      "Specification-Vendor" -> "org.npmaven",
      "Specification-Title" -> pkg.name,
      "Implementation-URL" -> "http://repo.npmaven.org/",
      "Specification-Version" -> pkg.version
    )

  private def manifestFile(entries:ListMap[String, String]):List[String] =
    entries.map{ case (k, v) => k+": "+v }.toList

  private def writeManifest(mf:List[String], jar:JarOutputStream):Unit = {
    jar.putNextEntry(new JarEntry("META-INF/"))
    jar.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"))
    val pStream = new PrintWriter(new OutputStreamWriter(jar, "ISO-8859-1"))
    val print = pStream.println(_:String)
    mf foreach print
    pStream.flush()
    // Do not close the pStream, as it will close the jar
  }

  private def mf(pkg:Package, jar:JarOutputStream):Unit = {
    val entries = manifest(pkg)
    val lines   = manifestFile(entries)
    writeManifest(lines, jar)
  }

  private def extractContents(pkg:Package, content:Array[Byte]):ListMap[List[String], Array[Byte]] = {
    val tar = new TarArchiveInputStream(new GZIPInputStream(new ByteArrayInputStream(content)))

    val pathToContents = Stream.continually {
      val entry = tar.getNextEntry
      val bytes = if(entry != null) Some(IOUtils.toByteArray(tar)) else None
      (entry, bytes)
    }.takeWhile(_._1 != null)
      .filterNot(_._1.isDirectory)
      .filter   (_._2.isDefined)
      .map { case (entry, bytes) => (entry.getName.split('/').tail.toList, bytes.get)
    }

    ListMap(pathToContents:_*)
  }

  private def pkgToJar(pkg:Package, content:Array[Byte]):Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val jar = new JarOutputStream(out)
    val jarPrint = new PrintStream(jar)
    val contents = extractContents(pkg, content)

    // Add manifest
    mf(pkg, jar)

    val root = "META-INF/resources/org/npmaven/"+pkg.name

    // Get main.bower from bower.json
    val pkgWithBower = contents
      .find(_._1.lastOption == Some("bower.json"))
      .flatMap { case (path, bytes) => Bower(bytes) }
      .map( b => pkg.copy(mainBower = Some(b.main)))
      .getOrElse(pkg)

    // Add props file
    jar.putNextEntry(new JarEntry(s"$root/package.properties"))
    pkgWithBower.asProperties.list(jarPrint)
    jarPrint.flush()

    // Add each file from the downloaded npm contents
    contents.foreach { case(pathList, bytes) =>
      val path = pathList.mkString("/")
      jar.putNextEntry(new JarEntry(s"$root/$path"))
      jar write bytes
    }

    jar.flush()
    jar.close()
    out.toByteArray
  }
}
