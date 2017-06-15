package scado.tests

import java.io.ByteArrayInputStream
import java.util.stream.IntStream

import org.scalatest._
import scado.ThrottlingInputStream


class ThrottlingInputStreamSpec extends FunSpec with Matchers {
  describe("The ThrottlingInputStream") {
    it("should not exceed target rate") {
      val testBytesLength = 1000
      val testStream = new ByteArrayInputStream(
        IntStream.generate(() => 0).limit(testBytesLength).toArray.map(_.toByte))

      val stream = new ThrottlingInputStream(testStream, testBytesLength / 2)
      val (_, elapsedTime) = timed {
        var b = stream.read()
        while (b != -1) {
          b = stream.read()
        }
      }

      // Slight fluctuations are expected due to wall clock precision
      // of System.currentTimeMillis() and possible GC activities
      elapsedTime should be >= 1.98d
    }
  }

  private def timed[R](code: => R, t: Long = System.nanoTime): (R, Double) =
    (code, (System.nanoTime - t) / 1e9)
}
