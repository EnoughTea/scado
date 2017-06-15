package scado

import java.io.File
import java.net._
import java.nio.file._
import java.util.concurrent.{ConcurrentHashMap, Executors, TimeUnit}

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.io._
import scala.util._

import org.apache.commons.io.FileUtils

/**
  * Main entry point for the application.
  */
object ConsoleApp extends App {
  val conf = new CmdArgumentsConf(args)
  implicit val executorService = Executors.newFixedThreadPool(conf.threadCount())
  implicit val executorContext = ExecutionContext.fromExecutorService(executorService)

  val speedLimit = conf.bpsLimit()
  val speedLimitPerThread = speedLimit / conf.threadCount()
  if (!conf.outputFolder().exists()) conf.outputFolder().mkdir()

  println(s"Downloading links from '${conf.linksFile()}' into '${conf.outputFolder()}' using " +
    s"${conf.threadCount()} thread(s) with ${speedLimit / 1024} Kb/s speed limit " +
    s"(${speedLimitPerThread / 1024} Kb/s per thread).")
  downloadLinksFromFile(conf.linksFile())

  try {
    executorContext.shutdown()
    executorContext.awaitTermination(1, TimeUnit.MINUTES)
  } catch {
    case e: Exception => println(e.printStackTrace())
  }


  /**
    * Downloads all files from the given links file asynchronously.
    *
    * @param linksFilePath Links file path.
    * @return Sequence of download results for every URL in the links file.
    */
  def downloadLinksFromFile(linksFilePath: File): Iterator[Try[DownloadResult]] = {
    require(linksFilePath != null)
    require(linksFilePath.exists())

    val downloadedFiles = new ConcurrentHashMap[URL, File]()
    val linksLinesWithLineNumbers = Source.fromFile(linksFilePath)
      .getLines()
      .map(_.trim)
      .zipWithIndex

    val downloadLinksTask = for {
      (line, lineNumber) <- linksLinesWithLineNumbers
    } yield {
      singleLinkDownloadTask(line, lineNumber, downloadedFiles)
    }

    Await.result(Future.sequence(downloadLinksTask), Duration.Inf)
  }

  /**
    * Creates a task to download a single file from the given links file line.
    *
    * @param line            Links file line.
    * @param lineNumber      Links file line number for error reporting.
    * @param downloadedFiles Map of all files previosly downloaded.
    * @return Task to download a single file from the given links file line.
    */
  def singleLinkDownloadTask(line: String,
                             lineNumber: Int,
                             downloadedFiles: ConcurrentHashMap[URL, File]): Future[Try[DownloadResult]] = Future {
    require(line != null)
    require(lineNumber >= 0)
    require(downloadedFiles != null)

    val validation = validateLinkFileLine(line, lineNumber)
    validation match {
      case Success(dlInfo) =>
        val (url, localFile) = (dlInfo.url, dlInfo.localFile)
        val alreadyDownloadedFile = downloadedFiles.get(url)
        if (alreadyDownloadedFile == null) {
          performDownloading(url, localFile, downloadedFiles)
        } else {
          performCopying(url, alreadyDownloadedFile, localFile)
        }
      case Failure(e) => Failure(e)
    }
  }

  /**
    * Extracts url and file path from the links file line.
    *
    * @param line       Line from the links file.
    * @param lineNumber Number of this line in the links file.
    * @return [[DownloadInfo]] if url and file path were successfully extracted from the line;
    *         reason for failure otherwise.
    */
  def validateLinkFileLine(line: String, lineNumber: Int): Try[DownloadInfo] = try {
    require(line != null)
    require(lineNumber >= 0)

    val firstSpace = line.indexOf(' ')
    if (firstSpace > 0) {
      val rawUrl = line.substring(0, firstSpace)
      val decodedUrl = URLDecoder.decode(rawUrl, "UTF-8")
      val url = new URL(decodedUrl)
      val filePath = Paths.get(conf.outputFolder().getAbsolutePath, line.substring(firstSpace + 1))
      Success(new DownloadInfo(url, filePath.toFile))
    } else throw new Exception(s"Malformed line $lineNumber: $line")
  } catch {
    case e: Throwable => Failure(e)
  }


  private def performDownloading(url: URL,
                                 localFile: File,
                                 downloadedFiles: ConcurrentHashMap[URL, File]): Try[DownloadResult] = {
    println(s"Downloading '$url' ...")
    val elapsedTime = Download.file(url, localFile, speedLimitPerThread)
    downloadedFiles.putIfAbsent(url, localFile)
    println(f"Downloaded '$url' in $elapsedTime%.2f seconds " +
      f"with average speed of ${localFile.length / elapsedTime / 1024}%.2f Kb/s.")
    Success(new DownloadResult(url, localFile, elapsedTime))
  }

  private def performCopying(url: URL,
                             previouslyDownloadedFile: File,
                             localFile: File): Try[DownloadResult] = {
    val (_, elapsedTime) = Download.timed {
      // File copy can be synchronous, since this method is already called on a separate thread.
      FileUtils.copyFile(previouslyDownloadedFile, localFile)
    }
    println(f"Copied '${previouslyDownloadedFile.getName}' to '${localFile.getName}' in $elapsedTime%.3f seconds.")
    Success(new DownloadResult(url, localFile, elapsedTime))
  }


  /**
    * Contains information about file to download: its URL and local destination.
    *
    * @param url       URL with file to download.
    * @param localFile Local destination.
    */
  class DownloadInfo(val url: URL, val localFile: File)

  /**
    * Contains result of a single file download.
    *
    * @param info        Download info.
    * @param elapsedTime How much time the download took.
    */
  class DownloadResult(val info: DownloadInfo, val elapsedTime: Double) {
    def this(url: URL, localFile: File, elapsedTime: Double) = this(new DownloadInfo(url, localFile), elapsedTime)
  }

}
