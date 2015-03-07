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
          <a href="http://github.com/joescii/npmaven" class="fork nav"></a>
          <h1 id="npmaven">npmaven<a href="#npmaven" class="header-link"><span class="header-link-content">&nbsp;</span></a></h1>
        </body>
      </html>

      Pamflet(template) must ==/ (template)
    }

    "replace all +'s in an anchor before a hash with %2B's" in {
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

    "NOT replace any +'s in an anchor after the hash" in {
      def template(href:String) = <html>
        <head></head>
        <body>
          <div><a href={href}>npmaven</a></div><ul class="outline"> <li> <a href={href+"#Community"}>Community </a> </li><li> <a href={href+"#Build+Status"}>Build Status </a> </li> </ul><ol class="toc"> <li class="generated"><div class="current">Contents in Depth</div></li><li class="generated"><div><a href={href}>Combined Pages</a></div></li> </ol>
        </body>
      </html>

      Pamflet(template("Combined+Pages.html")) must ==/ (template("Combined%2BPages.html"))
    }

    "prepend all css sources with 'site'" in {
      val h1Before = "css/blueprint/screen.css"
      val h2Before = "css/blueprint/grid.css"
      val h3Before = "css/blueprint/print.css"

      val h1After = "site/css/blueprint/screen.css"
      val h2After = "site/css/blueprint/grid.css"
      val h3After = "site/css/blueprint/print.css"

      def template(href1:String, href2:String, href3:String) = <html>
        <head>
          <link rel="stylesheet" href={href1} type="text/css" media="screen, projection"/>
          <link rel="stylesheet" href={href2} type="text/css" media="screen and (min-device-width: 800px), projection"/>
          <link rel="stylesheet" href={href3} type="text/css" media="print"/>
        </head>
        <body></body>
      </html>

      Pamflet(template(h1Before, h2Before, h3Before)) must ==/ (template(h1After, h2After, h3After))
    }

    "prepend all js sources with 'site'" in {
      val src1Before = "js/jquery-1.6.2.min.js"
      val src2Before = "js/jquery.collapse.js"
      val src3Before = "js/pamflet.js"

      val src1After = "site/js/jquery-1.6.2.min.js"
      val src2After = "site/js/jquery.collapse.js"
      val src3After = "site/js/pamflet.js"

      def template(src1:String, src2:String, src3:String) = <html>
        <head>
          <script type="text/javascript" src={src1}></script>
          <script type="text/javascript" src={src2}></script>
          <script type="text/javascript" src={src3}></script>
          <script type="text/javascript">
            Pamflet.page.language = 'en';
          </script>
        </head>
        <body></body>
      </html>

      Pamflet(template(src1Before, src2Before, src3Before)) must ==/ (template(src1After, src2After, src3After))
    }

    "prepend all img sources with 'site'" in {
      val srcBefore = "img/fork.png"
      val srcAfter = "site/img/fork.png"

      def template(src:String) = <html>
        <head></head>
        <body>
          <a href="http://github.com/joescii/npmaven" class="fork nav"><img src={src} alt="Fork me on GitHub"/></a>
        </body>
      </html>

      Pamflet(template(srcBefore)) must ==/ (template(srcAfter))
    }

    "replace all 'pamplet.html' in an anchor with '.'" in {
      def template(href:String) = <html>
        <head></head>
        <body>
          <a class="page prev nav" href={href}>
            <span class="space">&nbsp;</span>
            <span class="flip arrow">❧</span>
          </a>
          <a class="page next nav" href="SomethingElse.html">
            <span class="space">&nbsp;</span>
            <span class="arrow">❧</span>
          </a>
          <div><a href={href}>npmaven</a></div><ol class="toc"> <li class="generated"><div class="current">Contents in Depth</div></li><li class="generated"><div><a href="SomethingElse.html">Combined Pages</a></div></li> </ol>
          <div><a href={href}>npmaven</a></div><ul class="outline"> <li> <a href={href+"#Community"}>Community </a> </li><li> <a href={href+"#BuildStatus"}>Build Status </a> </li> </ul><ol class="toc"> <li class="generated"><div class="current">Contents in Depth</div></li><li class="generated"><div><a href="SomethingElse.html">Combined Pages</a></div></li> </ol>
        </body>
      </html>

      Pamflet(template("npmaven.html")) must ==/ (template("."))
    }
  }

}
