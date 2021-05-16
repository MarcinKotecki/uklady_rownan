import Main.{readMatrixFromFile, time, timeList}
import Solver.{parallelism, solveParallel, solveSequential}
import org.scalatest.FunSuite
import org.scalatest.Matchers.convertToAnyShouldWrapper

class TimeTests extends FunSuite {

  val path = "/Workspace/studia/sem6/baz/uklady_rownan/res/"

  test("test calculations") {
    val (_, _, a) = readMatrixFromFile(path + "A")
    val (_, _, b) = readMatrixFromFile(path + "b")
    val it: Int = 3

    val res = solveSequential(a, b, it)
    println(res)
    val res2 = solveParallel(a, b, it)
    println(res2)

    res shouldBe Seq(-5.0, 5.0, 2.0)
    res2 shouldBe Seq(-5.0, 5.0, 2.0)
  }

  test("time 800x800") {
    val (_, _, a) = readMatrixFromFile(path + "800x800.txt")
    val (_, _, b) = readMatrixFromFile(path + "1x800.txt")
    val it: Int = 10
    val repeat: Int = 20

    for (_ <- 0 to repeat) {
      val res = time("seq", "total")(solveSequential(a, b, it))
      println(res)
    }
    for (p <- 0 to 4) {
      parallelism = Math.pow(2, p).toInt
      for (_ <- 0 to repeat) {
        val res = time("par", "total")(solveParallel(a, b, it))
        println(res)
      }
    }

    timeList
      .groupBy(x => x._type + x.parallelism)
      .map(y => (y._1, y._2.groupBy(z => z.stage)
                           .map(zz => (zz._1, zz._2.map(a => a.time).sum / repeat))
                           .toSeq.sortBy(st => st._1)))
      .toSeq.sortBy(ts => ts._1)
      .foreach(t => {
        print(t._1)
        t._2.foreach(tt => print("\t" + tt._1 + "\t" + tt._2))
        println
      })

  }

}
