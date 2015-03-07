package org.npmaven.snippet

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

import scala.xml.Elem

object PamfletTests extends Specification with XmlMatchers {
  "Pamflet object" should {
    "not change an anchor without +'s" in {
      val template = <html>
        <head></head>
        <body>
          <a href="http://github.com/joescii/npmaven" class="fork nav"><img src="img/fork.png" alt="Fork me on GitHub"/></a>
        </body>
      </html>

      Pamflet(template) must ==/ (template)
    }

    "replace all +'s in an anchor with %2B's" in {
      val a1Before = <a href="Contents+in+Depth.html">Contents in Depth</a>
      val a2Before = <a href="Combined+Pages.html">Combined Pages</a>
      val a1After = <a href="Contents%2Bin%2BDepth.html">Contents in Depth</a>
      val a2After = <a href="Combined%2BPages.html">Combined Pages</a>

      def template(a1:Elem, a2:Elem) = <html>
        <head></head>
        <body>
          <div class="current">npmaven</div> <ol class="toc">
          <li class="generated">
            <div>
              {a1}
            </div>
          </li> <li class="generated">
            <div>
              {a2}
            </div>
          </li>
        </ol>
      </body>
      </html>

      Pamflet(template(a1Before, a2Before)) must ==/ (template(a1After, a2After))
    }
  }

}
