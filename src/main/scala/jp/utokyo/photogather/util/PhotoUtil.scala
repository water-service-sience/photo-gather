package jp.utokyo.photogather.util

import com.drew.imaging.ImageMetadataReader
import java.io.{BufferedInputStream, ByteArrayInputStream}
import com.drew.metadata.exif.GpsDirectory
import com.drew.lang.Rational
import jp.utokyo.photogather.model.{User, Photo}

/**
 * Created with IntelliJ IDEA.
 * User: takezou
 * Date: 12/11/25
 * Time: 17:56
 * To change this template use File | Settings | File Templates.
 */
object PhotoUtil {



  def extractGpsInfo(data : Array[Byte]) : Option[(Double,Double)] = {
    def rationalToDouble(rArray: Array[Rational]) = {
      rArray(0).doubleValue + rArray(1).doubleValue / 60.0 + rArray(2).doubleValue / (60 * 60 * 1000)
    }

    val stream = new BufferedInputStream( new ByteArrayInputStream(data))

    val metadata = ImageMetadataReader.readMetadata(stream,false)
    val dir = metadata.getDirectory(classOf[GpsDirectory])
    val lon = try {
      dir.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF) match {
        case "E" => rationalToDouble(dir.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE))
        case "W" => -rationalToDouble(dir.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE))
        case null => return None
      }
    } catch {
      case e: Exception => {
        return None
      }
    }

    val lat = try {
      dir.getString(GpsDirectory.TAG_GPS_LATITUDE_REF) match {
        case "N" => rationalToDouble(dir.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE))
        case "S" => -rationalToDouble(dir.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE))
        case null => return None
      }
    } catch {
      case e: Exception => {
        return None
      }
    }

    Some(lat -> lon)
  }

}
