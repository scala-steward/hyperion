package hyperion.rest

import java.time.{LocalDate, Month, OffsetDateTime}
import java.time.format.DateTimeFormatter.{ISO_DATE, ISO_OFFSET_DATE_TIME}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.Materializer
import hyperion.database.HistoricalMeterReading
import hyperion.UsageCalculationActor.UsageDataRecord
import spray.json._

/** Allows easy mix-in of [[HyperionJsonProtocol]] */
trait HyperionJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit def meterReadingFormat: RootJsonFormat[MeterReading] = HyperionJsonProtocol.meterReadingFormat
  implicit def historicalMeterReadingFormat: RootJsonFormat[HistoricalMeterReading] = HyperionJsonProtocol.historicalMeterReadingFormat
  implicit def usageDataRecordFormat: RootJsonFormat[UsageDataRecord] = HyperionJsonProtocol.usageDataRecordFormat
  implicit def localDateUnmarshaller: Unmarshaller[String, LocalDate] = HyperionJsonProtocol.localDateUnmarshaller
  implicit def monthUnmarshaller: Unmarshaller[String, Month] = HyperionJsonProtocol.monthUnmarshaller
}

/** Converts the model of Hyperions REST API into JSON and back */
object HyperionJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit object LocalDateFormat extends JsonFormat[LocalDate] {
    override def read(json: JsValue): LocalDate = json match {
      case JsString(value) => Try(LocalDate.parse(value, ISO_DATE)) match {
        case Success(result) => result
        case Failure(cause)  => deserializationError(s"Cannot convert $value to a LocalDate", cause)
      }
      case thing: JsValue    => deserializationError(s"Cannot convert $thing to a LocalDate")
    }

    override def write(input: LocalDate): JsValue = JsString(
      input.format(ISO_DATE)
    )
  }

  implicit object OffsetDateTimeFormat extends JsonFormat[OffsetDateTime] {
    override def read(json: JsValue): OffsetDateTime = json match {
      case JsString(value) => Try(OffsetDateTime.parse(value, ISO_OFFSET_DATE_TIME)) match {
        case Success(result) => result
        case Failure(cause)  => deserializationError(s"Cannot convert $value to a OffsetDateTime", cause)
      }
      case thing: JsValue    => deserializationError(s"Cannot convert $thing to a OffsetDateTime")
    }

    override def write(input: OffsetDateTime): JsValue = JsString(
      input.format(ISO_OFFSET_DATE_TIME)
    )
  }

  implicit val localDateUnmarshaller: Unmarshaller[String, LocalDate] = new Unmarshaller[String, LocalDate] {
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[LocalDate] = {
      Try(LocalDate.parse(value, ISO_DATE)) match {
        case Success(date) => Future { date }
        case Failure(reason) => Future.failed(reason)
      }
    }
  }

  implicit val monthUnmarshaller: Unmarshaller[String, Month] = new Unmarshaller[String, Month] {
    override def apply(value: String)(implicit ec: ExecutionContext, materializer: Materializer): Future[Month] = {
      Try(value.toInt).map(Month.of) match {
        case Success(month) => Future { month }
        case Failure(reason) => Future.failed(reason)
      }
    }
  }

  val historicalMeterReadingFormat: RootJsonFormat[HistoricalMeterReading] = jsonFormat4(HistoricalMeterReading)
  val meterReadingFormat: RootJsonFormat[MeterReading] = jsonFormat9(MeterReading)
  val usageDataRecordFormat: RootJsonFormat[UsageDataRecord] = jsonFormat4(UsageDataRecord)
}