import scala.io.Source
import scala.util.Using

/** Checks if all dates are in ascending order inside the file
  */
@main
def validate(fileName: String): Unit =
  Using.resource(Source.fromFile(fileName)): source =>

    val iterator = source.getLines()

    if iterator.hasNext then
      val combined = iterator.zip(iterator.drop(1))

      val result = combined.forall { (less, greater) =>
        val isBeforeOpt =
          for
            rec1 <- Record.readRecord(less)
            rec2 <- Record.readRecord(greater)
          yield rec1.date.isBefore(rec2.date)

        isBeforeOpt.getOrElse(false)
      }

      println(s"File is ${if (result) "valid" else "invalid"}")
    else println(s"File is empty")
