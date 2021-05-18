//case class Matrix(w: Integer, h: Integer, values: Array[Double]) {
//  def get(x: Integer, y: Integer): Double =
//    values(y * w + x)
//
//  def update(x: Integer, y: Integer, value: Double): Unit =
//    values.update(y * w + x, value)
//
//  def copy(): Matrix =
//    Matrix(w, h, values.clone)
//
//  def -(second: Matrix): Matrix =
//    Matrix(w, h, values.zip(second.values).map { case (a, b) => a - b })
//
//  def +(second: Matrix): Matrix =
//    Matrix(w, h, values.zip(second.values).map { case (a, b) => a + b })
//
//  def *(second: Matrix): Matrix = {
//    val newMatrix = Matrix(second.w, h, Array.ofDim[Double](second.w * h))
//    for {r <- 0 until h
//         c <- 0 until second.w}
//      newMatrix.update(c, r, (0 until w).map(i => get(i, r) * second.get(c, i)).sum)
//    newMatrix
//  }
//
//  def prettyPrint(): Unit = {
//    println()
//    for (y <- 0 until h) {
//      print("[\t")
//      for (x <- 0 until w)
//        print(values(y * w + x) + "\t")
//      print("]\n")
//    }
//  }
//}