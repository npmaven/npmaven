package bootstrap.liftweb

import java.io.InputStream

import org.npmaven.rest.NpmRest
import org.npmaven.snippet.Pamflet

import net.liftweb._
import http._
import net.liftweb.sitemap._
import Loc._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("org.npmaven")

    // Build SiteMap
    val entries = List(
      // Allows everything in /site (created by Pamflet) to be exposed.
      Menu(Loc("npmaven", new Link(List("site"), true), "npmaven"))
    )
    LiftRules.setSiteMap(SiteMap(entries:_*))

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // For fixing the +'s in Pamflet's URLs
    LiftRules.contentParsers = List(
      ContentParser(
        Seq("html", "xhtml", "htm"),
        (content:InputStream) => S.htmlProperties.htmlParser(content),
        Pamflet(_)
      )
    )

    LiftRules.statelessRewrite.prepend {
      // Point / at the npmaven.html from Pamflet
      case RewriteRequest(ParsePath("index" :: Nil, ext, _, _), _, _) =>
        RewriteResponse("site" :: "npmaven" :: Nil, "html")

      // Slaps /site on the front of most everything
      case RewriteRequest(ParsePath(head :: path, "html", _, _), _, _)
        if head != "site" // To avoid infinite recursion
      => RewriteResponse("site" :: head :: path, "html")

      // Slaps /site on the front of the version files (or any html file that has extra periods for that matter)
      // It seems that the extra periods throws off parsing, so ext == "" in this case
      // Furthermore, having html on the end screws it up. Stripping it seems to work great.
      case RewriteRequest(ParsePath(head :: path, ext, _, _), _, _)
        if head.endsWith(".html")
      => println(head::path); RewriteResponse("site" :: head.substring(0, head.length-5) :: path, ext)

    }

    LiftRules.statelessDispatch.append(NpmRest)
  }
}