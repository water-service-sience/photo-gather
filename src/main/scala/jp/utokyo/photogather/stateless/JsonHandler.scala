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

      val photos = Photo.findNearBy(lat,lon,areaSize)

      import net.liftweb.json._
      import net.liftweb.json.JsonDSL._

      val format = new SimpleDateFormat("MM/dd HH:mm")

      JArray(photos.map( p => {
        ("latitude" -> p.latitude.is) ~
        ("longitude" -> p.longitude.is) ~
        ("img" -> toImageUrl(p.resourceKey.is)) ~
        ("place" -> p.place.is) ~
        ("ownerId" -> p.user.is) ~
        ("owner" -> p.user.obj.open_!.nickname.is) ~
        ("captured" -> format.format(p.captured.is))

      }))



    }
  }

}
