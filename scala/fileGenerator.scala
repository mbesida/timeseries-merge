import java.nio.file.Files
import java.time.LocalDate
import scala.util.Random
import scala.util.Using

val OutputFolder = os.pwd / "data"

@main
def generate(): Unit =
  if os.exists(OutputFolder) then
    println(s"Removing directory ${OutputFolder.toString}")
    os.remove.all(OutputFolder)
    
  println(s"Creating directory ${OutputFolder.toString}")
  os.makeDir(OutputFolder)
  for idx <- (1 to 100)
  do
    val date = LocalDate.of(1900, 1, 1).plusYears(idx)
    val file = OutputFolder / s"file$idx.dat"

    Using.resource(
      Files.newBufferedWriter((OutputFolder / s"file$idx.dat").wrapped)
    ): out =>
      for i <- (1 to 500000)
      do
        val record = Record(date.plusDays(i), Random.nextInt(100) + 1)
        out.write(Record.stringify(record) + System.lineSeparator())
