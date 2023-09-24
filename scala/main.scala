//> using scala "3.3.1"
//> using toolkit latest


import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scala.annotation.tailrec
import scala.io.{Codec, Source}
import scala.util.Try


object Main  {
  import Record._

  private val MaxNumberOfFiles = 100

  def main(args: Array[String]): Unit = {
    if (args.size < 2) {
      println("""Usage:
                | - first argument is output file name
                | - if second argument is a directory all files in that directory are considered as timeseries data
                |   and are processed by the program, but no more than 100 files
                | - if second argument isn't a directory then second and all other arguments are considered as file names to process""".stripMargin)
    } else {
      Files.deleteIfExists(Paths.get(args.head)) //delete output file if it exists

      val inputFiles = if (Files.isDirectory(Paths.get(args(1)))) {
        new File(args(1)).listFiles().withFilter(!_.isDirectory).map(f =>  s"${f.getParent}/${f.getName}").take(MaxNumberOfFiles)
      } else args.tail.distinct.take(MaxNumberOfFiles)

      if (inputFiles.nonEmpty) {
        println(s"Start merging  following files: ${inputFiles.mkString(", ")}")
        val output = new BufferedWriter(new FileWriter(args.head, true))

        val iterators = inputFiles.map(file => Source.fromFile(file, Codec.UTF8.name).getLines())

        @tailrec
        def rec(filesData: Seq[(Iterator[String], Record)]): Unit = {
          if (filesData.nonEmpty) {
            //sort read lines by date
            val sortedBydate = filesData.groupBy(_._2.date).toSeq.sortWith{case ((date1, _), (date2, _)) => date1.isBefore(date2)}
            val reduced = reduceRecords(sortedBydate)

            (reduced.toList: @unchecked) match {
              case head :: tail => {
                if (head._2.isDefined) newRecord(output, head._2.get)
                val refreshedTail = refreshIterators(tail)
                if (head._1.hasNext) {
                  readRecord(head._1.next()) match {
                    case Some(record) => rec((head._1, record) :: refreshedTail)
                    case None => rec(refreshedTail)
                  }
                } else rec(refreshedTail)
              }
            }
          }
        }

        rec(refreshIterators(iterators.toList.map((_, None))))

        output.close()
      } else {
        println("There are no input files to process")
      }
    }


  }

  private def newRecord(output: BufferedWriter, record: Record): Unit = {
    output.write(stringify(record)); output.newLine()
  }

  private def refreshIterators(list: List[(Iterator[String], Option[Record])]): List[(Iterator[String], Record)] = list.collect {
    case (iterator, None) if iterator.hasNext => (iterator, readRecord(iterator.next()))
    case (iterator, x@Some(r)) => (iterator, x)
  }.collect { case (iterator, Some(record)) => (iterator, record) }

  private def reduceRecords(sortedByDate: Seq[(LocalDate, Seq[(Iterator[String], Record)])]): Seq[(Iterator[String], Option[Record])] = {
    //reduce values of identical dates(None means keep file iterator while its read record was reduced)
    sortedByDate.flatMap { case (date, arr) =>
      val reducedValue = Some(arr.map(_._2).reduce((acc, record) => acc.add(record)))
      (arr.head._1, reducedValue) +: arr.tail.map { case (p, _) => (p, None) }
    }
  }
}


