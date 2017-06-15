package scado

import java.io._
import java.net._

import resource._

/**
  * Static class containing download logic.
  */
object Download {
  /**
    * Downloads the specified remote file and saves its contents to the given local file.
    *
    * @param remoteFileUrl URL of the file to download.
    * @param outputFile    Local file where downloaded bytes will be saved.
    * @param speedLimitBps Download speed limit.
    * @throws java.io.IOException Some kind of IO error.
    * @return Amount of time in seconds it took to download the given file.
    */
  @throws(classOf[IOException])
  def file(remoteFileUrl: URL, outputFile: File, speedLimitBps: Int): Double = {
    require(remoteFileUrl != null)
    require(outputFile != null)

    val connection = remoteFileUrl.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    val (_, elapsedTime) = timed {
      for {
        input <- managed(new ThrottlingInputStream(new BufferedInputStream(connection.getInputStream), speedLimitBps))
        output <- managed(new BufferedOutputStream(new FileOutputStream(outputFile)))
      }
        Stream.continually(input.read())
          .takeWhile(_ != -1)
          .foreach(b => output.write(b))
    }

    elapsedTime
  }

  private[scado] def timed[R](code: => R, t: Long = System.nanoTime): (R, Double) =
    (code, (System.nanoTime - t) / 1e9)
}
