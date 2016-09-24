package com.mbesida.timeseries

import scala.io.Source

/**
  * Checks if all dates are in ascending order inside the file
  */
object Validator extends App {
  if (args.size != 1) {
    println("Usage: the only one program argument is the file name to be validated")
  } else {
    val iterator = Source.fromFile(args.head).getLines()

    if (iterator.hasNext) {
      val result = iterator.zip(iterator.drop(1)).forall{case (less, greater) =>
        (for {
          rec1 <- Record.readRecord(less)
          rec2 <- Record.readRecord(greater)
        } yield rec1.date.isBefore(rec2.date)).getOrElse(false)
      }
      println(s"File is ${if (result) "valid" else "invalid"}")
    } else {
      println(s"File is empty")
    }
  }
}
