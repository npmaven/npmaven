package org.npmaven
package artifacts

import java.io._

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

  private def extractContents(pkg:Package, content:Array[Byte]):PackageContents = {
    val tar = new TarArchiveInputStream(new GZIPInputStream(new ByteArrayInputStream(content)))
    logger.trace("Looking for "+pkg.main)

    Stream.continually {
      val entry = tar.getNextEntry
      val bytes = if(entry != null) Some(IOUtils.toByteArray(tar)) else None
      (entry, bytes)
    }.takeWhile(_._1 != null)
      .filterNot(_._1.isDirectory)
      .foldLeft(PackageContents()) { case (pc, (entry, bytes)) =>
      val name = entry.getName.split('/').last

      if(name == "bower.js") pc.copy(bower = bytes)
      else if(Some(name) == pkg.main) pc.copy(main = bytes)
      else pc
    }

  }

  private def pkgToJar(pkg:Package, content:Array[Byte]):Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val jar = new JarOutputStream(out)
    val contents = extractContents(pkg, content)

    // Add manifest
    mf(pkg, jar)

    // Add package directory
    jar.putNextEntry(new JarEntry("org/"))
    jar.putNextEntry(new JarEntry("org/npmaven/"))
    jar.putNextEntry(new JarEntry("org/npmaven/"+pkg.name+"/"))

    contents.main.zip(pkg.main).foreach { case(b,main) =>
      logger.trace("b.length == "+b.length)
      jar.putNextEntry(new JarEntry("org/npmaven/"+pkg.name+"/"+main))
      jar write b
    }

    jar.flush()
    jar.close()
    out.toByteArray
  }
}

case class PackageContents(
  main: Option[Array[Byte]] = None,
  min: Option[Array[Byte]] = None,
  map: Option[Array[Byte]] = None,
  bower: Option[Array[Byte]] = None,
  pkg: Option[Array[Byte]] = None
)