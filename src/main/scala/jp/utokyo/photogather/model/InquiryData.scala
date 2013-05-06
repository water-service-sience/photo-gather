package jp.utokyo.photogather.model
import net.liftweb.mapper._

object InquiryData extends InquiryData with LongKeyedMetaMapper[InquiryData] with CRUDify[Long, InquiryData]{


  def searchByWord(word : String) = {
    List(comment,detail,situationInField,cause,support,messageToArea,memoToArea,messageToOrg,memoToArea).flatMap( f => {
      findAll(Like(f,"%" + word + "%"))
    }).toSet.toList
  }

}
class InquiryData extends LongKeyedMapper[InquiryData] with IdPK{

  def getSingleton = InquiryData

  object operationId extends MappedString(this,128)
  object inputDate extends MappedDateTime(this){
    override def dbIndexed_? = true
  }
  object personInCharge extends MappedString(this,128)
  object place extends MappedLongForeignKey(this,Place){
    override def dbIndexed_? = true

    override def dbColumnName: String = "placeId"
  }
  object category extends MappedLongForeignKey(this,Category){
    override def dbIndexed_? = true

    override def dbColumnName: String = "categoryId"
  }
  object comment extends MappedText(this){
    override def dbColumnName: String = "comment"
  }
  object detail extends MappedText(this)
  object situationInField extends MappedText(this)
  object cause extends MappedText(this)
  object support extends MappedText(this)
  object messageToArea extends MappedText(this)
  object memoToArea extends MappedText(this)
  object messageToOrg extends MappedText(this)
  object memoToOrg extends MappedText(this)

  def categoryName = {
    category.obj.map(_.name.is).openOr("None")
  }

  def placeName = {
    place.obj.map(_.name.is).openOr("None")
  }

}