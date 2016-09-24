package com.mbesida.timeseries

import java.io.PrintWriter
import java.time.LocalDate

import scala.util.Random

object FileGenerator extends App {
  (1 to 100) foreach { idx =>
    val date = LocalDate.of(1900, 1, 1).plusYears(idx)
    val out = new PrintWriter("input/data"+ idx + ".dat")
    (1 to 500000) foreach { i =>
      val record = Record(date.plusDays(i), Random.nextInt(100) + 1)
      out.write(Record.stringify(record)); out.println()
    }
  }
}
