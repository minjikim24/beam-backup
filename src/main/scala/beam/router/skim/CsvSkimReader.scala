package beam.router.skim

import beam.router.skim.core.{AbstractSkimmerInternal, AbstractSkimmerKey}
import com.typesafe.scalalogging.Logger
import com.univocity.parsers.common.record.Record
import com.univocity.parsers.csv.{CsvParser, CsvParserSettings}
import org.matsim.core.utils.io.IOUtils

import java.io.{BufferedReader, File}
import java.util
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * Skims csv reader.
  *
  * @param aggregatedSkimsFilePath path to skims file
  * @param fromCsv                 column mapping function (depends on skimmer type)
  * @param logger                  passing logger from skimmer as this is invoked by various skimmers. This helps distinguishing in the
  *                                log which skimmer has an issue reading skims.
  */
class CsvSkimReader[Key <: AbstractSkimmerKey, Value <: AbstractSkimmerInternal](
  val aggregatedSkimsFilePath: String,
  fromCsv: scala.collection.Map[String, String] => (Key, Value),
  logger: Logger
) {

  def readAggregatedSkims: Map[Key, Value] = {
    if (!new File(aggregatedSkimsFilePath).isFile) {
      logger.info(s"warmStart skim NO PATH FOUND '$aggregatedSkimsFilePath'")
      Map.empty
    } else {
      val skimsTryMap = Try {
        IOUtils.getBufferedReader(aggregatedSkimsFilePath)
      }.flatMap(tryReadSkims)
      skimsTryMap match {
        case Success(skimMap) => skimMap
        case Failure(ex) =>
          logger.warn(s"Could not load warmStart skim from '$aggregatedSkimsFilePath'", ex)
          Map.empty
      }
    }
  }

  def readSkims(reader: BufferedReader): Map[Key, Value] = {
    tryReadSkims(reader).recover { case ex: Throwable =>
      logger.warn(s"Could not read warmStart skim from '$aggregatedSkimsFilePath'", ex)
      Map.empty[Key, Value]
    }.get
  }

  private def tryReadSkims(reader: BufferedReader): Try[Map[Key, Value]] = {
    val csvParser: CsvParser = getCsvParser
    val result: Try[Map[Key, Value]] = Try {
      // Headers will be available only when parsing was started
      lazy val headers = {
        csvParser.getRecordMetadata.headers()
      }
      val mapReader = csvParser.iterateRecords(reader).asScala
      val res: Map[Key, Value] = mapReader
        .map(rec => {
          val a = convertRecordToMap(rec, headers)
          val newPair = fromCsv(a)
          newPair
        })
        .toMap
      logger.info(s"warmStart skim (${res.size} map size) successfully loaded from path '$aggregatedSkimsFilePath'")
      res
    }
    Try(csvParser.stopParsing())
    result
  }

  private def convertRecordToMap(rec: Record, header: Array[String]): scala.collection.Map[String, String] = {
    val res = new util.HashMap[String, String]()
    rec.fillFieldMap(res, header: _*)
    res.asScala
  }

  private def getCsvParser: CsvParser = {
    val settings = new CsvParserSettings()
    settings.setHeaderExtractionEnabled(true)
    settings.detectFormatAutomatically()
    val csvParser = new CsvParser(settings)
    csvParser
  }

}
