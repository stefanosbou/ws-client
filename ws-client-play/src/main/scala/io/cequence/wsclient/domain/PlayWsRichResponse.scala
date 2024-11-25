package io.cequence.wsclient.domain

import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSRequest

case class PlayWsRichResponse(
  playWsResponse: Option[PlayWsResponse],
  status: StatusData,
  headers: Map[String, Seq[String]]
) extends RichResponse {
  override def response: Option[Response] = playWsResponse
}

final case class PlayWsResponse(
  rawResponse: StandaloneWSRequest#Response,
  serviceNameForLogging: String,
  endpointForLogging: Option[String]
) extends Response {

  override def json: JsValue =
    try {
      rawResponse.body[JsValue]
    } catch {
      case _: JsonParseException =>
        throw new CequenceWSException(
          s"${serviceNameForLogging} - ${endpointForLogging.getOrElse("N/A")}: '${rawResponse.body}' is not a JSON."
        )
      case _: JsonMappingException =>
        throw new CequenceWSException(
          s"${serviceNameForLogging} - ${endpointForLogging.getOrElse("N/A")}: '${rawResponse.body}' is an unmappable JSON."
        )
    }

  override def string: String = rawResponse.body

  override def source: Source[ByteString, _] = rawResponse.bodyAsSource
}
