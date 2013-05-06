package jp.utokyo.photogather.model
import net.liftweb.mapper._

object Category extends Category with LongKeyedMetaMapper[Category] with CRUDify[Long, Category]{


}
class Category extends LongKeyedMapper[Category] with IdPK{

  def getSingleton = Category

  object name extends MappedString(this,128)
}