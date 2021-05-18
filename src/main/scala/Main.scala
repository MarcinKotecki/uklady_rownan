
import Solver.solveParallel

import scala.collection.mutable.ListBuffer

object Main {

  val PRINT_TIME = false

  var timeList: ListBuffer[TimeRecord] = ListBuffer()

  case class TimeRecord(_type: String, parallelism: Int, stage: String, time: Long)

  def time[T](_type: String, stage: String)(f: => T): T = {
    val t1 = System.currentTimeMillis()
    val result = f
    val t2 = System.currentTimeMillis()
    if (PRINT_TIME)
      println("time " + _type + " " + (if (_type == "seq") -1 else Solver.parallelism) + " " + stage + " " + (t2 - t1))
    timeList += TimeRecord(
      _type,
      if (_type == "seq") -1 else Solver.parallelism,
      stage,
      t2 - t1
    )
    result
  }

  def readMatrixFromFile(f: String): (Int, Int, Seq[Double]) = {
    val seq = scala.io.Source.fromFile("/home/marcin/" + f).getLines().flatMap(s => s.split(" +")).toArray
    (
      seq.head.toInt,
      seq(1).toInt,
      seq.slice(2, seq.length).map(v => v.toDouble)
    )
  }

  def main(args: Array[String]): Unit = {
    val (_, _, a) = readMatrixFromFile(args(1))
    val (_, _, b) = readMatrixFromFile(args(2))
    val it: Int = 100

    val res = time("seq", "total")(solveParallel(a, b, it))
    println(res)
  }

}