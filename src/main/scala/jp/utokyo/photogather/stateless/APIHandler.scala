package jp.utokyo.photogather.stateless

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import jp.utokyo.photogather.util.StorageUtil
import jp.utokyo.photogather.model.{Photo, User}
import net.liftweb.common.{Logger, Full}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import jp.utokyo.photogather.function.PhotoFunction
import java.util.Date

/**
 * For client
 * User: takeshita
 * DateTime: 12/11/13 1:58
 */
object APIHandler extends RestHelper  {
  val logger = Logger(getClass())

  val AccessKeyHeader = "PU-AccessKey"

  def loginUser = {

    println("Headers ----")
    S.request.open_!.headers.foreach(println(_))
    val accessKey = S.getRequestHeader(AccessKeyHeader).open_!
    User.findByAccessKey(accessKey).open_!
  }

  serve {
    case Req("api" :: "status" :: Nil,_,GetRequest) => {
      PlainTextResponse("ok")
    }
    case  r @ Req("api" :: "user" :: "create":: Nil, _, PostRequest) =>{//api" :: "user" :: "create" :: Nil JsonPost json => {
      val body = r.json.open_!

      val JString(nickname) = (body \ "nickname")
      val user = User.createUser(nickname)

      ("userId" -> user.id.is.toString) ~
      ("accessKey" -> user.accessKey.is)

    }
    case "api" :: "login" :: Nil JsonGet _ => {

      val username = S.param("username").open_!
      val password = S.param("password").open_!

      User.findByUsername(username) match{
        case Full(user) => {
          if (user.password == password){
            ("userId" -> user.id.is.toString) ~
            ("accessKey" -> user.accessKey.is)
          }else{
            InMemoryResponse("Bad password".getBytes, Nil, Nil, 400)
          }
        }
        case _ => {
          InMemoryResponse("Bad password".getBytes, Nil, Nil, 400)
        }
      }
    }



    case Req("api" :: "upload" :: Nil,_,PostRequest) => {

      val user = loginUser
      val file = S.request.open_!.uploadedFiles(0)

      val accessKey : String = user.accessKey

      User.findByAccessKey(accessKey) match{
        case Full(user) => {
          val photo = PhotoFunction.saveToStorage(user, file.fileName,file.file)

          ("result" -> "ok") ~
          ("photoId" -> photo.id.is)
        }
        case _ => {

          InMemoryResponse("Unknown user".getBytes, Nil, Nil, 401)
        }
      }
    }
    case r @ Req("api" :: "photo" :: "edit" :: photoId :: Nil, _ , PostRequest) => {
      val body = r.json.open_!

      val user = loginUser

      val photo = Photo.findByKey(photoId.toLong).open_!

      if(photo.user.is != user.id.is){
        throw new Exception("Can't edit not own photo")
      }

      def extract( key : String, func : String => Any){
        (body \ key).toOpt match{
          case Some(JString(v)) => func(v)
          case Some(JInt(v)) => func(v.toString())
          case Some(JDouble(v)) => func(v.toString())
          case _ => // nothing
        }
      }
      def extractD( key : String, func : Double => Any){
        (body \ key).toOpt match{
          case Some(JString(v)) => func(v.toDouble)
          case Some(JInt(v)) => func(v.toDouble)
          case Some(JDouble(v)) => func(v)
          case _ => // nothing
        }
      }
      extract("place", v => photo.place := v)
      extract("comment", v => photo.comment := v)
      if(!photo.hasGpsInfo.is){
        extractD("lon",lon => {
          extractD("lat",lat => {
            photo.longitude := lon
            photo.latitude := lat
            photo.hasGpsInfo := true;
          })
        })
      }

      photo.save()

      ("result" -> "ok") ~
      ("photoId" -> photoId)
    }

    case Req("api" :: "photo" :: "uploaded" :: Nil, _ , GetRequest) => {
      val user = loginUser
      val photo = Photo.findSelfUploaded(user.id.is)

      photoListToJSON(photo)
    }

    case Req("api" :: "photo" :: "near" :: Nil, _ , GetRequest) => {
      val user = loginUser

      val lat = S.param("lat") or S.param("latitude") open_!
      val lon = S.param("lon") or S.param("lng") or S.param("longitude") open_!
      val from = S.param("time_from").map(t => new Date(t.toLong)) openOr Photo.before24hours()

      val photos = Photo.findNearBy(lat.toDouble,lon.toDouble,1,from);

      photoListToJSON(photos)



    }

  }


  implicit def dateToJValue(d : Date) = JString(d.getTime.toString)

  def photoListToJSON(photos : List[Photo]) : JValue = {
    photos.map(photo => {
      ("id" -> photo.id.is) ~
      ("resouceKey" ->  photo.resourceKey.is) ~
        ("ownerId" -> photo.user.is) ~
        ("place" -> photo.place.is) ~
        ("comment" -> photo.comment.is) ~
        ("captured" -> photo.captured.is)  ~
        ("hasGps" -> photo.hasGpsInfo.is)  ~
        ("latitude" -> photo.latitude.is) ~
        ("longitude" -> photo.longitude.is)
    })
  }

}
