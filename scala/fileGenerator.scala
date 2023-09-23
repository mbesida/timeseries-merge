import java.io.PrintWriter
import java.time.LocalDate

import scala.util.Random
import java.nio.file.Files
import scala.util.Using
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileVisitResult

val OutputFolder = "data"
@main
def generate(): Unit =
  val dirPath = Paths.get(OutputFolder)

  if Files.exists(dirPath) then
    Files.walkFileTree(dirPath, DeleteDirectoryVisitor)
  else Files.createDirectory(dirPath)

  for idx <- (1 to 100)
  do
    val date = LocalDate.of(1900, 1, 1).plusYears(idx)
    Using.resource(
      new PrintWriter(s"${dirPath.toAbsolutePath().toString()}/file$idx.dat")
    ): out =>
      for i <- (1 to 500000)
      do
        val record = Record(date.plusDays(i), Random.nextInt(100) + 1)
        out.write(Record.stringify(record) + System.lineSeparator())

private object DeleteDirectoryVisitor extends SimpleFileVisitor[Path] {
  override def visitFile(
      file: Path,
      attrs: BasicFileAttributes
  ): FileVisitResult = {
    Files.delete(file)
    FileVisitResult.CONTINUE
  }

  override def visitFileFailed(
      file: Path,
      exc: java.io.IOException
  ): FileVisitResult = {
    FileVisitResult.CONTINUE
  }

  override def postVisitDirectory(
      dir: Path,
      exc: java.io.IOException
  ): FileVisitResult = {
    Files.delete(dir)
    FileVisitResult.CONTINUE
  }
}
