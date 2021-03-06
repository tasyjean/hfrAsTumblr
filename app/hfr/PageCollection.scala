package hfr

import play.api.Logger
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin

import org.joda.time.DateTime

import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

object PageCollection {

  def db = ReactiveMongoPlugin.db
  def collection = db("pages")

  def saveOrUpdate(page: Page) {
    val futurePage = findHead(page)

    futurePage onSuccess {
      case result => result match {
        case None => println("No match found"); save(page)
        case Some(p: Page) => println("Found one match"); update(p)
      }
    }
  }

  def save(page: Page): scala.concurrent.Future[reactivemongo.core.commands.LastError] = {
    Logger.debug("save page " + page)
    implicit val writer = PageBSON.Writer
    collection.insert(page)
  }

  def update(page: Page): scala.concurrent.Future[reactivemongo.core.commands.LastError] = {
    Logger.debug("update page " + page)

    val selector = BSONDocument("topicId" -> BSONString(page.topicId), "pageNumber" -> BSONInteger(page.pageNumber))
    val modifier = BSONDocument(
      "$set" -> BSONDocument(
        "updatedAt" -> BSONDateTime(new DateTime().getMillis),
        "icons" -> BSONArray(page.icons.map { s => BSONString(s) }: _*),
        "images" -> BSONArray(page.images.map { s => BSONString(s) }: _*)
      ),
      "$inc" -> BSONDocument("nbViews" -> BSONInteger(1))
    )

    collection.update(selector, modifier)
  }


  def findHead(page: Page): Future[Option[Page]] = {
    findHeadByTopicIdAndPageNumber(page.topicId, page.pageNumber)
  }

  def findHeadByTopicIdAndPageNumber(topicId: String, pageNumber: Int): Future[Option[Page]] = {
    Logger.debug(s"find head for $topicId and $pageNumber")
    implicit val reader = PageBSON.Reader
    val query = BSONDocument("topicId" -> BSONString(topicId), "pageNumber" -> BSONInteger(pageNumber))

    val cursor = collection.find(query)
    cursor.headOption
  }

}
