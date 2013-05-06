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
    case "monthList" => monthList _
    case "dayList" => dayList _
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

          <li data-image={JsonHandler.toImageUrl(p.resourceKey.is)} onClick="selectImage(this);">{p.comment}
          <img src={JsonHandler.toImageUrl(p.resourceKey.is)} style="width:50px;height:50px;" />
          </li>
        })
        }
        </ul></div>
      </div>
    })

  }

  val days = List(0,31,28,31,30,31,30,31,31,30,31,30,31)

  def monthList(node : NodeSeq) : NodeSeq = {
    val now = Calendar.getInstance()
    val year = S.param("year").map(_.toInt).openOr(now.get(Calendar.YEAR))
    val month = S.param("month").map(_.toInt).openOr(now.get(Calendar.MONTH) + 1)
    val photos = photoListOfMonth(year,month)
    val dateList = (1 to days(month)).map( d => "%02d/%02d".format(month,d)).toList

    val firstDay = Calendar.getInstance()
    firstDay.set(year,month - 1,1)
    val padding = (0 until (firstDay.get(Calendar.DAY_OF_WEEK) - 1 )).map("empty" + _).toList

    var weekOfMonth = 0
    (padding ::: dateList).sliding(7,7).map( week => {
      weekOfMonth += 1
      val list = week.map(date => {

        if (date.startsWith("empty")){


          <div id={date} class="cal-day">
            <div class="label">--</div>
            <div class="body">
            </div>
          </div>

        }else{



        <div id={date} class="cal-day">
          <div class="label">{date}</div>
          <div class="body">
          {
            photos.get(date) match{
              case Some(uploaded) => {
                val p = uploaded(0)
                val img =
                    <a href={"uploaded_map?date=" + year + "/" + date}>
                      <img src={JsonHandler.toImageUrl(p.resourceKey.is)} style="width:50px;height:50px;" />
                    </a>
                if(uploaded.size >= 2){
                  Seq(img, <div>{"他" + (uploaded.size - 1) + "件"}</div>)
                }else{
                  img
                }
              }
              case None => <span>投稿なし</span>
            }
          }
          </div>
        </div>
        }
      })

      <div id={"week" + weekOfMonth}>{list}</div>
    }).toSeq


  }


  def dayList(node : NodeSeq) : NodeSeq = {
    val (year,month,day) = {
      val s = S.param("date").map(_.split("/")).open_!
      (s(0).toInt,s(1).toInt,s(2).toInt)
    }
    val photos = Photo.findInDay(year,month,day)

    photos.map(p => {

      <span>
        <div>{p.comment.is}</div>
        <img data-coord={p.latitude.is + ":" + p.longitude.is} class="thumb" src={JsonHandler.toImageUrl(p.resourceKey.is)} style="width:90px;height:90px;" />
      </span>
    })

  }



  def photoListOfMonth( year : Int , month : Int) = {
    val photos = Photo.findInMonth(year,month)
    val f = new SimpleDateFormat("MM/dd")
    val grouped = photos.groupBy(photo => f.format(photo.uploaded.is))

    grouped
  }


}
