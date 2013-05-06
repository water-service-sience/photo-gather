package jp.utokyo.photogather.model

import net.liftweb.mapper._
import java.util.{Calendar, Date}

/**
 * 
 * PhotoSnippet: takeshita
 * DateTime: 12/11/13 1:31
 */
object Photo  extends Photo with LongKeyedMetaMapper[Photo] with CRUDify[Long, Photo]{
  override def dbIndexes =  List(Index(Photo.hasGpsInfo, Photo.latitude,Photo.longitude))

  def findSelfUploaded(userId : Long) = {
    findAll(By(Photo.user,userId),OrderBy(Photo.captured,Descending))
  }

  def findNearBy( latitude : Double, longitude : Double, area : Double, after : Date = before24hours()) : List[Photo] = {
    findAll(
      By(Photo.hasGpsInfo,true),
      By_<=(Photo.latitude,latitude + area),By_>=(Photo.latitude,latitude - area),
      By_<=(Photo.longitude,longitude + area),By_>=(Photo.longitude,longitude - area))
  }

  def findBadPhotos() = {
    findAll(
      By_<=(Photo.goodness,-50),
      By_>=(Photo.captured,before24hours())
    )
  }

  def before24hours() = {
    new Date(new Date().getTime - 24 * 60 * 60 * 1000)
  }


  def findBetween(begin : Date,end : Date) : List[Photo] = {
    findAll(By_>=(Photo.captured,begin),By_<=(Photo.captured,end))
  }

  def findInMonth(year : Int, month : Int) = {

    val cal = Calendar.getInstance()
    cal.set(year,month - 1,1)
    val end = Calendar.getInstance()
    end.set(year,month,1)
    end.add(Calendar.DAY_OF_YEAR,-1)
    findBetween(cal.getTime,end.getTime)

  }

  def findInDay(year : Int , month : Int,day : Int) = {
    val cal = Calendar.getInstance()
    cal.set(year,month - 1,day,0,0)
    val end = Calendar.getInstance()
    end.set(year,month -1 , day + 1, 0,0)
    findBetween(cal.getTime,end.getTime)
  }


}


class Photo extends LongKeyedMapper[Photo] with IdPK{
  def getSingleton = Photo

  object user extends MappedLongForeignKey(this,User)


  object resourceKey extends MappedString(this,128)

  object uploaded extends MappedDateTime(this){
    override def defaultValue = new Date()
  }

  object captured extends MappedDateTime(this){
    override def defaultValue = new Date()
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

  object goodness extends MappedInt(this){
    override def defaultValue = 0
  }

  object comment extends MappedText(this){
    override def defaultValue = ""
  }

}
