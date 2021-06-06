import scala.collection.mutable.ListBuffer

object Timer {

  val PRINT_TIME = false

  var timeList: ListBuffer[TimeRecord] = ListBuffer()

  case class TimeRecord(_type: String, parallelism: Int, stage: String, time: Long)

  def time[T](_type: String, stage: String)(f: => T): T = {
    val t1 = System.currentTimeMillis()
    val result = f
    val t2 = System.currentTimeMillis()
    if (PRINT_TIME)
      println("time " + _type + " " + (if (_type == "seq") 0 else Solver.parallelism) + " " + stage + " " + (t2 - t1))
    timeList += TimeRecord(
      _type,
      if (_type == "seq") 0 else Solver.parallelism,
      stage,
      t2 - t1
    )
    result
  }

}
