package scado

import java.io._

/**
  * A simple decorator for [[InputStream]] that slows down reading in order to enforce given speed.
  *
  * @param underlying     Wrapped underlying [[InputStream]].
  * @param bytesPerSecond Reading speed limit in b/s. Values <= 0 means no speed limit.
  */
class ThrottlingInputStream(val underlying: InputStream, var bytesPerSecond: Int) extends InputStream {
  require(underlying != null)

  private[this] val per: Double = 1d // unit: seconds
  private[this] var allowance: Double = 0 // unit: bytes
  private[this] var lastCheckTime: Double = System.currentTimeMillis() / 1000d // unit: seconds

  @throws(classOf[IOException])
  override def available(): Int = underlying.available()

  override def close(): Unit = {
    underlying.close()
  }

  override def mark(readlimit: Int): Unit = synchronized {
    underlying.mark(readlimit)
  }

  override def markSupported(): Boolean = underlying.markSupported()

  @throws(classOf[IOException])
  override def read(): Int = {
    ratedRead(() => underlying.read(), 1)
  }

  private def ratedRead[T](action: () => T, allowanceCost: Double): T = {
    require(action != null)
    require(allowanceCost > 0)

    // Rate <= 0 means no limit, so exit early.
    val rate = bytesPerSecond
    if (rate <= 0) {
      return action()
    }

    // Token bucket algoritm:
    val currentTime = System.currentTimeMillis() / 1000d
    val timePassed = currentTime - lastCheckTime
    lastCheckTime = currentTime
    allowance += timePassed * (rate / per)
    if (allowance > rate) {
      allowance = rate
    }

    if (allowance < 1.0) {
      val sleepTime = (1 - allowance) * (per / rate)
      val sleepMs = sleepTime * 1000d
      Thread.sleep(sleepMs.toLong)
      lastCheckTime += sleepTime
      allowance = 0
      action()
    }
    else {
      allowance -= allowanceCost
      action()
    }
  }

  @throws(classOf[IOException])
  override def read(bytes: Array[Byte]): Int = {
    ratedRead(() => underlying.read(bytes), bytes.length)
  }

  @throws(classOf[IOException])
  override def read(bytes: Array[Byte], offset: Int, length: Int): Int = {
    ratedRead(() => underlying.read(bytes, offset, length), length)
  }

  @throws(classOf[IOException])
  override def reset(): Unit = synchronized {
    underlying.reset()
  }

  override def skip(n: Long): Long = underlying.skip(n)
}