//> using scala "3.3.1"
//> using lib "com.lihaoyi::os-lib:0.9.1"

import java.io.BufferedWriter
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.annotation.nowarn
import scala.annotation.tailrec
import scala.io.Codec
import scala.io.Source
import scala.util.Using

object Main:
  import Record._

  private val MaxNumberOfFiles = 100

  def main(args: Array[String]): Unit = {
    if (args.size < 2) {
      println(
        """Usage:
                | - first argument is output file name
                | - if second argument is a directory all files in that directory are considered as timeseries data
                |   and are processed by the program, but no more than 100 files
                | - if second argument isn't a directory then second and all other arguments are considered as file names to process""".stripMargin
      )
    } else {
      val target = os.pwd / args(0)
      os.remove(target) // delete output file if it exists

      val source = os.pwd / args(1)

      val inputFiles =
        if os.isDir(source) then
          os.list(source).filter(os.isFile).take(MaxNumberOfFiles)
        else
          val files = args.tail.distinct.map(os.pwd / _).take(MaxNumberOfFiles)
          files.filter(path => os.exists(path) && os.isFile(path)).toIndexedSeq

      if inputFiles.nonEmpty then
        println(
          s"Start merging  following files: ${inputFiles.map(_.toNIO.getFileName()).mkString(", ")}"
        )

        @tailrec
        def rec(
            out: BufferedWriter,
            filesData: Seq[(Iterator[String], Record)]
        ): Unit = {
          if filesData.nonEmpty then
            // sort read lines by date
            val sortedBydate = filesData.groupBy(_(1).date).toSeq.sortWith {
              case ((date1, _), (date2, _)) => date1.isBefore(date2)
            }
            val reduced = reduceRecords(sortedBydate)

            (reduced.toList: @nowarn) match {
              case (it, Some(r)) :: tail =>
                newRecord(out, r)
                val refreshedTail = refreshIterators(tail)
                if it.hasNext then
                  readRecord(it.next()) match
                    case Some(record) => rec(out, (it, record) :: refreshedTail)
                    case None         => rec(out, refreshedTail)
                else rec(out, refreshedTail)
            }
        }

        Using.resource(Files.newBufferedWriter(target.toNIO)) { out =>
          val iterators = inputFiles.map: file =>
            Source.fromFile(file.toIO, Codec.UTF8.name).getLines()

          rec(out, refreshIterators(iterators.toList.map((_, None))))
        }
      else println("There are no input files to process")
    }

  }

  private def newRecord(output: BufferedWriter, record: Record) =
    output.write(stringify(record))
    output.newLine()

  private def refreshIterators(
      list: List[(Iterator[String], Option[Record])]
  ): List[(Iterator[String], Record)] = list
    .collect {
      case (iterator, None) if iterator.hasNext =>
        (iterator, readRecord(iterator.next()))
      case (iterator, x @ Some(r)) => (iterator, x)
    }
    .collect { case (iterator, Some(record)) => (iterator, record) }

  private def reduceRecords(
      sortedByDate: Seq[(LocalDate, Seq[(Iterator[String], Record)])]
  ): Seq[(Iterator[String], Option[Record])] = 
    // reduce values of identical dates(None means keep file iterator while its read record was reduced)
    sortedByDate.flatMap: (date, arr) =>
      val reducedValue =
        Some(arr.map(_._2).reduce((acc, record) => acc.add(record)))
      (arr.head._1, reducedValue) +: arr.tail.map((p, _) => (p, None))
end Main
