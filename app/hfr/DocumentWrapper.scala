package hfr

import collection.mutable.ListBuffer

import org.jsoup.Jsoup
import org.jsoup.nodes._
import org.jsoup.select.Elements

import play.api.Logger

case class DocumentWrapper(url: String) {

  def getDocument(): Document = {
    Logger.debug("getDocument(%s)".format(url))

    Jsoup.connect(url)
      .data("query", "Java")
      .userAgent("Mozilla")
      .cookie("auth", "token")
      .timeout(3000)
      .get()
  }

  def listElements(cssSelector: String, attributeName: String): List[String] = {
    var buffer: ListBuffer[String] = ListBuffer()

    val elments: Elements = getDocument().select(cssSelector)
    val it = elments.iterator()
    while (it.hasNext) {
      val img: Element = it.next()
      buffer += img.attr(attributeName)
    }

    buffer.toList
  }
}