package jp.utokyo.photogather.snippet

import net.liftweb.http.{S, StatefulSnippet}
import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import jp.utokyo.photogather.function.PhotoFunction
import jp.utokyo.photogather.model.{InquiryData, User}
import java.text._

/**
 * Created with IntelliJ IDEA.
 * User: takezoux2
 * Date: 2013/05/06
 * Time: 21:59
 * To change this template use File | Settings | File Templates.
 */
class InquirySnippet extends StatefulSnippet{
  def dispatch: DispatchIt = {
    case "searchBox" => searchBox _
    case "searchResult" => searchResult _
    case "detail" => detail _
  }


  def searchBox(node : NodeSeq) : NodeSeq = {


    <hoge></hoge>
  }

  def searchResult(node : NodeSeq) : NodeSeq = {
    if(S.param("w").isDefined && S.param("w").open_!.length > 0){
      val word = S.param("w").open_!

      val inquiries = InquiryData.searchByWord(word)

      <table>
        <thead><tr><th>ID</th><th>支線</th><th>概要</th></tr></thead>
        <tbody>
        {
        inquiries.map(inq => {
          <tr>
            <td><a href={"detail?id=" + inq.id.toString}>{inq.id.toString()+"(詳細)"}</a></td>
            <td>{inq.placeName}</td>
            <td>{inq.categoryName}</td>
          </tr>
        })


        }</tbody>
      </table>

    }else{
      <div>No results</div>
    }
  }

  def detail(node : NodeSeq) : NodeSeq = {

    val id = S.param("id").open_!.toLong
    val inq = InquiryData.findByKey(id).open_!
    val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm")

    bind("e",node,
      "id" -> inq.id.is.toString,
      "inputdate" -> dateFormat.format(inq.inputDate.is),
      "operator" -> inq.personInCharge.is,
      "category" -> inq.categoryName,
      "place" -> inq.placeName,
      "comment" -> inq.comment.is,
      "detail" -> inq.detail.is,
      "situation" -> inq.situationInField.is,
      "cause" -> inq.cause.is,
      "messagetoarea" -> inq.messageToArea.is,
      "memotoarea" -> inq.memoToArea.is,
      "messagetoorg" -> inq.messageToOrg.is,
      "memotoorg" -> inq.memoToOrg.is
    )

  }


}
