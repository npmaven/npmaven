package org.npmaven.snippet

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._

object Pamflet {
  def apply(html:NodeSeq):NodeSeq = ("a" #> { anchor:NodeSeq =>
    val href = (anchor \ "@href").toString()
    val replace = "a [href]" #> href.replaceAllLiterally("+", "%2B")
    replace(anchor)
  }).apply(html)
}
