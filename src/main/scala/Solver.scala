import Timer.time
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object Solver {

  implicit val system: ActorSystem = ActorSystem("Sys")

  var parallelism: Int = Runtime.getRuntime.availableProcessors()

  def mult1(a: Seq[Double], size: Int)(b: (Seq[Double], Int)): Seq[Double] =
    b._1.map(y => y * a(b._2 / size * (size + 1)))

  def mult2(a: Seq[Double], b: Seq[Double], size: Int)(id: Int): Double =
    a((size + 1) * id) * b(id)

  def mult2b(a: Seq[Double], b: Seq[Double], size: Int)(r: Range): Seq[Double] =
    r.map(id => a((size + 1) * id) * b(id))

  def mult3(a: Seq[Double])(b: Seq[Double]): Double =
    b.zipWithIndex.map(y => y._1 * a(y._2)).sum

  def asFuture[A, B](f: A => B)(x: A): Future[B] = Future(f(x))

  def solveSequential(A: Seq[Double], b: Seq[Double], it: Int): Seq[Double] = {
    val size = b.length
    val LplusU = Seq(A).flatten.zipWithIndex.map { case (x, id) => if ((id / size) != (id % size)) x else 0.0 }
    val Dinv = Seq(A).flatten.zipWithIndex.map { case (x, id) => if ((id / size) == (id % size)) 1 / x else 0.0 }
    val minDinv = Dinv.map(x => -x)

    // m1 = -D^(-1) * (L + U)
    val m1 = time("seq", "m1") {
      LplusU.grouped(size).zipWithIndex.toSeq.flatMap(mult1(minDinv, size)).grouped(size).toSeq
    }

    // m2 = D^(-1) * b
    val m2 = time("seq", "m2") {
      (0 until size).map(mult2(Dinv, b, size))
    }

    // x(k+1) = m1 * x(k) + m2
    time("seq", "m3") {
      var res = Array.ofDim[Double](size).toSeq
      for (_ <- 0 until it) {
        res = m1.map(mult3(res))
        res = (0 until size).map(id => res(id) + m2(id))
      }
      res
    }
  }

  def solveParallel(A: Seq[Double], b: Seq[Double], it: Int): Seq[Double] = {
    val size = b.length
    val LplusU = Seq(A).flatten.zipWithIndex.map { case (x, id) => if ((id / size) != (id % size)) x else 0.0 }
    val Dinv = Seq(A).flatten.zipWithIndex.map { case (x, id) => if ((id / size) == (id % size)) 1 / x else 0.0 }
    val minDinv = Dinv.map(x => -x)

    // m1 = -D^(-1) * (L + U)
    val m1 = time("par", "m1") {
      Await.result(
        Source(LplusU.grouped(size).zipWithIndex.toSeq)
          .mapAsync(parallelism)(asFuture(mult1(minDinv, size)))
          .toMat(Sink.seq)(Keep.right)
          .run(),
        Duration.Inf).flatten.grouped(size).toSeq
    }

    // m2 = D^(-1) * b
    val m2 = time("par", "m2") {
      Await.result(
        Source(
          (0 until parallelism).map(q => Range(q * size / parallelism, (q + 1) * size / parallelism))
        )
          .mapAsync(parallelism)(asFuture(mult2b(Dinv, b, size)))
          .toMat(Sink.seq)(Keep.right)
          .run(),
        Duration.Inf).flatten
    }

    // x(k+1) = m1 * x(k) + m2
    time("par", "m3") {
      var res = Array.ofDim[Double](size).toSeq
      val src = Source(m1)
      for (_ <- 0 until it) {
        res = Await.result(
          src
            .mapAsync(parallelism)(asFuture(mult3(res)))
            .toMat(Sink.seq)(Keep.right)
            .run(),
          Duration.Inf)
        res = (0 until size).map(id => res(id) + m2(id))
      }
      res
    }

  }

}
