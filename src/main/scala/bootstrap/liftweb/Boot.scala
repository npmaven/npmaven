package bootstrap.liftweb

import java.io.InputStream

import net.liftweb._
import org.npmaven.rest.NpmRest
import org.npmaven.snippet.Pamflet
import util._
import Helpers._

import common._
import net.liftweb.http._
import sitemap._
import Loc._
import net.liftmodules.JQueryModule
import net.liftweb.http.js.jquery._

import scala.xml.NodeSeq

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
      Menu(Loc("npmaven", new Link(List("site"), true), "npmaven")
      )
    )

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery191
    JQueryModule.init()

    // For correcting the +'s in Pamflet's links
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
      case RewriteRequest(ParsePath(head :: path, ext, _, _), _, _)
        if head != "site" // To avoid infinite recursion
        && head != "repo" // To avoid screwing up the repos
      => RewriteResponse("site" :: head :: path, ext)
    }

    LiftRules.statelessDispatch.append(NpmRest)
  }
}