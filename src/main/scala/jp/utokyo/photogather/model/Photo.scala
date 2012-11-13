package jp.utokyo.photogather.model

import net.liftweb.mapper._
import java.util.Date

/**
 * 
 * PhotoSnippet: takeshita
 * DateTime: 12/11/13 1:31
 */
object Photo  extends Photo with LongKeyedMetaMapper[Photo] with CRUDify[Long, Photo]{

}


class Photo extends LongKeyedMapper[Photo] with IdPK{
  def getSingleton = Photo

  object user extends MappedLongForeignKey(this,User)


  object resourceKey extends MappedString(this,128)

  object uploaded extends MappedDateTime(this){
    override def defaultValue = new Date()
  }

  object captured extends MappedDateTime(this){
    override def defaultValue = new Date(0)
  }

  object hasGpsInfo extends MappedBoolean(this){
    override def defaultValue = false
  }

  object latitude extends MappedDouble(this){
    override def defaultValue = 0
  }

  object longitude extends MappedDouble(this){
    override def defaultValue = 0
  }

  object comment extends MappedText(this){
    override def defaultValue = ""
  }

}
