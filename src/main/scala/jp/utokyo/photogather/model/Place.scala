package jp.utokyo.photogather.model
import net.liftweb.mapper._

object Place extends Place with LongKeyedMetaMapper[Place] with CRUDify[Long, Place]{


}
class Place extends LongKeyedMapper[Place] with IdPK{

  def getSingleton = Place

  object name extends MappedString(this,128)
  object latitude extends MappedDouble(this)
  object longitude extends MappedDouble(this)
}