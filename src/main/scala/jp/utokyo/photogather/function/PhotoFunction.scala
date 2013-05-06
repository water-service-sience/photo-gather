package jp.utokyo.photogather.function

import jp.utokyo.photogather.util.{PhotoUtil, StorageUtil, EncryptUtil}
import jp.utokyo.photogather.model.{User, Photo}

/**
 * 
 * User: takeshita
 * DateTime: 12/12/05 1:28
 */
object PhotoFunction {
  val AvailableExtensions = Set("jpeg","jpg","gif","png")

  def saveToStorage(user : User,_filename : String,data : Array[Byte]) = {

    val ext = _filename.substring(_filename.lastIndexOf(".") + 1)

    if(!AvailableExtensions.contains(ext)){
      throw new Exception("Can't upload not photo file.(Wrong extension)")
    }
    val digest = EncryptUtil.sha1Digest(data)
    val filename = digest + "." + ext

    StorageUtil.save(filename,data)

    val photo = Photo.create
    photo.user(user)
    photo.resourceKey := filename

    PhotoUtil.extractGpsInfo(data) match{
      case Some((lat,lon)) => {
        photo.latitude := lat
        photo.longitude := lon
        photo.hasGpsInfo := true
      }
      case None =>
    }
    photo.save()
    photo
  }
}
