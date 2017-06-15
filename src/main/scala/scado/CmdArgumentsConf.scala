package scado

import java.io.File

import org.rogach.scallop._

/**
  * Configures and performs command-line argument parsing with Scallop (https://github.com/scallop/scallop/wiki).
  *
  * @param arguments Sequence of command-line arguments passed from the main entry point.
  */
class CmdArgumentsConf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val cwd: String = System.getProperty("user.dir")
  val threadCount: ScallopOption[Int] = opt[Int](short = 'n', validate = _ > 0, name = "n", argName = "thread count",
    default = Some(1), descr = "Number of simultaneous downloading threads")


  version(s"${_p.getImplementationTitle} ${_p.getImplementationVersion} © 2017 Vladimir Zakharov")
  banner(
    """Usage: scado [OPTIONS]
      |Options:
      |-n <number of threads> -f <file with newline-separated links>
      |-l <download speed limit> -o <folder for downloaded files>
      |""".stripMargin)
  val bpsLimit: ScallopOption[Int] = opt[Int](short = 'l', name = "l", argName = "b(kb, mb)/s", default = Some(0),
    descr = "Download rate limit in bytes/s, shared by all threads. Suffixes 'k' and 'm' can be used, like '256k'."
  )(_speedLimitConverter)
  val linksFile: ScallopOption[File] = opt[File](short = 'f', name = "f", argName = "file path", required = true,
    descr = "File with links to download.")
  val outputFolder: ScallopOption[File] = opt[File](short = 'o', name = "o", argName = "folder path",
    default = Some(new File(cwd)), descr = "Folder to save downloaded files.")

  private[this] lazy val _p = getClass.getPackage

  /**
    * Converter for the speed limit argument. Used to parse a string like "64k" into an amount of bytes like 65536.
    */
  private[this] lazy val _speedLimitConverter = singleArgConverter[Int](s => {
    val limitRgx = """^(\d+\s*)+(k|m)?$""".r
    val multiplierMap = Map("k" -> 1024, "m" -> 1024 * 1024)
    s.trim().toLowerCase() match {
      case "" => 0
      case limitRgx(bps) => bps.toInt
      case limitRgx(bps, suffix) => bps.toInt * multiplierMap.getOrElse(suffix, 1)
      case _ => throw new IllegalArgumentException(
        "-l argument should contain number of bytes per second with optional 'k' or 'm' suffix.")
    }
  })

  validateFileExists(linksFile)
  verify()
}

