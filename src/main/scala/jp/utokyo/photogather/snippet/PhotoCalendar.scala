package jp.utokyo.photogather.snippet

import net.liftweb.http.{S, SHtml, StatefulSnippet}
import xml.NodeSeq
import net.liftweb.util.Helpers
import Helpers._
import SHtml._
import jp.utokyo.photogather.util.{EncryptUtil, StorageUtil}
import jp.utokyo.photogather.model.{User, Photo}
import jp.utokyo.photogather.stateless.JsonHandler
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * 
 * User: takeshita
 * DateTime: 12/11/13 2:43
 */
class PhotoCalendar extends StatefulSnippet {
  def dispatch: DispatchIt = {
    case "list" => list _
  }

  def list(n : NodeSeq) : NodeSeq = {

    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY)
    cal.set(Calendar.HOUR,0)
    cal.set(Calendar.MINUTE,0)
    cal.set(Calendar.SECOND,0)
    cal.set(Calendar.MILLISECOND,0)

    val begin = cal.getTime
    cal.add(Calendar.DAY_OF_MONTH,7)
    val end = cal.getTime
    cal.add(Calendar.DAY_OF_MONTH,-7)


    val photos = Photo.findBetween(begin,end)

    val format = new SimpleDateFormat("MMdd")
    val disp = new SimpleDateFormat("M月d日")
    (0 until 7).map(i => {
      val b = cal.getTime
      cal.add(Calendar.DAY_OF_MONTH,1)
      val e = cal.getTime
      val date = format.format(b.getTime)
      val todays = photos.filter(p => {
        b.before(p.captured.is) && p.captured.is.before(e)
      })
      <div id={date} class="cal-day">
        <div class="label">{disp.format(b)}</div>
        <div class="body"><ul>
        {
        todays.map(p => {

          <li data-image={JsonHandler.toImageUrl(p.resourceKey.is)} onClick="selectImage(this);">{p.place}
          <img src={JsonHandler.toImageUrl(p.resourceKey.is)} style="width:50px;height:50px;" />
          </li>
        })
        }
        </ul></div>
      </div>
    })

  }


}
