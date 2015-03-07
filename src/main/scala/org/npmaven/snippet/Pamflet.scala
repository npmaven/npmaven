package org.npmaven.snippet

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._

object Pamflet {
  def apply(html:NodeSeq):NodeSeq =
    (fixAnchors andThen
      fixLinks andThen
      fixScripts andThen
      fixImgs
    ).apply(html)

  private val fixAnchors: NodeSeq => NodeSeq = ("a" #> { anchor:NodeSeq =>
    val href = (anchor \ "@href").toString()
    val replace = "a [href]" #> href
      .replaceAllLiterally("+", "%2B")     // Fix the links with +'s
      .replaceAll("^npmaven\\.html", ".") // Make npmaven.html links go to the root of this context
    replace(anchor)
  })

  private val fixLinks: NodeSeq => NodeSeq = ("link" #> { link:NodeSeq =>
    val href = (link \ "@href").toString()
    val replace = "link [href]" #> href
      .replaceAll("^css", "site/css")  // Prepend 'site' for all css assets
    replace(link)
  })

  private val fixScripts: NodeSeq => NodeSeq = ("script" #> { script:NodeSeq =>
    val src = (script \ "@src").toString()
    val replace = if(src.length > 0) "script [src]" #> src
      .replaceAll("^js", "site/js")   // Prepend 'site' for all js assets
    else identity[NodeSeq] _          // Don't cause a 'src' to be added to inline scripts
    replace(script)
  })

  private val fixImgs: NodeSeq => NodeSeq = ("img" #> { img:NodeSeq =>
    val src = (img \ "@src").toString()
    val replace = "img [src]" #> src
      .replaceAll("^img", "site/img")  // Prepend 'site' for all img assets
    replace(img)
  })
}
