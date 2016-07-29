package hyperion.database

import java.time.LocalDate

import org.slf4j.LoggerFactory
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object MeterReadingDAO extends DatabaseSupport with DateTimeColumns {
  private[this] val log = LoggerFactory.getLogger(getClass)

  case class MeterReading(recordDate: LocalDate,
                          gas: BigDecimal,
                          electricityNormal: BigDecimal,
                          electricityLow: BigDecimal)

  class MeterReadings(tag: Tag) extends Table[MeterReading](tag, "meter_readings") {
    def recordDate = column[LocalDate]("record_date", O.PrimaryKey)
    def gas = column[BigDecimal]("gas")
    def electricityNormal = column[BigDecimal]("electricity_normal")
    def electricityLow = column[BigDecimal]("electricity_low")

    def * = (recordDate, gas, electricityNormal, electricityLow) <> (MeterReading.tupled, MeterReading.unapply)
  }

  private[this] val meterReadings = TableQuery[MeterReadings]

  def recordMeterReading(value: MeterReading) = {
    val insert = DBIO.seq(meterReadings += value)
    db.run(insert) andThen {
      case Failure(t) =>
        log.error("Error while storing meter reading into database", t)
      case Success(v) =>
        log.info("Successfully stored meter reading into database")
    }
  }
}