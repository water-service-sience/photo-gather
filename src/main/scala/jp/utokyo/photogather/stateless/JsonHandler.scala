package jp.utokyo.photogather.stateless

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JString
import net.liftweb.http.S
import jp.utokyo.photogather.model.Photo
import java.text.SimpleDateFormat

/**
 * 
 * User: takeshita
 * DateTime: 12/11/13 1:58
 */
object JsonHandler extends RestHelper  {

  def toImageUrl(resourceKey : String) = {
    val req = S.request.open_!.request
    val context = req.contextPath
    val port = req.serverPort

    def portStr = if(port == 80) "" else ":" + port
    def contextStr = if(context.isEmpty) "" else "/" + context


    "http://" + req.serverName + portStr + contextStr + "/images/uploaded/" + resourceKey
  }

  serve {
    case "api" :: "photos" :: "nearBy" :: Nil JsonGet _ => {

      val lat = S.param("latitude").open_!.toDouble
      val lon = S.param("longitude").open_!.toDouble
      val zoom = S.param("zoom").openOr("3").toDouble
      val areaSize = zoom * 0.01

      val photos = {
        val date = S.param("day").openOr( "")
        if (date.length > 0){
          val (year,month,day) = {
            val s = date.split("/")
            (s(0).toInt,s(1).toInt,s(2).toInt)
          }
          Photo.findInDay(year,month,day)
        }else{
          Photo.findNearBy(lat,lon,areaSize)
        }
      }

      import net.liftweb.json._
      import net.liftweb.json.JsonDSL._

      val format = new SimpleDateFormat("MM/dd HH:mm")

      JArray(photos.map( p => {
        ("latitude" -> p.latitude.is) ~
        ("longitude" -> p.longitude.is) ~
        ("img" -> toImageUrl(p.resourceKey.is)) ~
        ("ownerId" -> p.user.is) ~
        ("owner" -> p.user.obj.open_!.nickname.is) ~
        ("captured" -> format.format(p.captured.is)) ~
        ("goodness" -> p.goodness.is)

      }))



    }
  }

}
