package io.cequence.wsclient.domain

import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.libs.json.JsValue

trait RichResponse {
  def response: Option[Response]

  // status code and message
  def status: StatusData

  def headers: Map[String, Seq[String]]
}

trait Response {
  def json: JsValue

  def string: String

  def source: Source[ByteString, _]
}

case class StatusData(
  code: Int,
  message: String
)
