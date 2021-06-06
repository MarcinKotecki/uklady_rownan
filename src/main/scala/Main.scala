
import Solver.solveParallel
import Timer.time

import java.io.FileWriter

object Main {

  def readMatrixFromFile(f: String): Seq[Double] = {
    val src = scala.io.Source.fromFile(f)
    val seq = src.getLines().flatMap(s => s.split(" +")).toArray
    src.close
    seq.slice(2, seq.length).map(v => v.toDouble)
  }

  def saveVectorToFile(f: String, v: Seq[Double]): Unit = {
    val writer = new FileWriter(f)
    writer.append('1').append('\n').append(v.length.toString).append('\n')
    v.foreach(vv => writer.append(vv.toString).append("\n"))
    writer.close()
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Należy wskazać 2 pliki z macierzami i nazwę pliku wynikowego")
      System.exit(1)
    }

    val a = readMatrixFromFile(args(0))
    val b = readMatrixFromFile(args(1))
    val x = args(2)
    val it: Int = if (args.length < 4) 100 else args(3).toInt

    val res = time("seq", "total")(solveParallel(a, b, it))
    println(res)
    saveVectorToFile(x, res);
    System.exit(0)
  }

}