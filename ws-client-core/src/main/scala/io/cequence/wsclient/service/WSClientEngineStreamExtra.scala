package io.cequence.wsclient.service

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import play.api.libs.json.JsValue

/**
 * Stream request support specifically tailored for OpenAI API.
 *
 * @since Feb
 *   2023
 */
trait WSClientEngineStreamExtra {

  protected implicit val materializer: Materializer

  def execJsonStream(
    endPoint: String,
    method: String,
    endPointParam: Option[String] = None,
    params: Seq[(String, Option[Any])] = Nil,
    bodyParams: Seq[(String, Option[JsValue])] = Nil
  ): Source[JsValue, NotUsed]
}
