import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

final case class Record(date: LocalDate, value: Int):
  def add(record: Record): Record =
    require(record.date.equals(this.date), "Dates should be equal")
    Record(date, value + record.value)

object Record:
  private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def readRecord(line: String): Option[Record] =
    line.split(":").toList match
      case date :: value :: Nil =>
        Try(Record(LocalDate.parse(date, dateFormat), value.toInt)).toOption
      case _ => None

  def stringify(record: Record): String =
    s"${record.date.format(dateFormat)}:${record.value.toString}"
