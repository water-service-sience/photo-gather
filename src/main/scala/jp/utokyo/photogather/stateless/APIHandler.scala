package jp.utokyo.photogather.stateless

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import jp.utokyo.photogather.util.StorageUtil
import jp.utokyo.photogather.model.{Photo, User}
import net.liftweb.common.{Logger, Full}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import jp.utokyo.photogather.function.PhotoFunction
import java.util.{Calendar, Date}
import java.text.SimpleDateFormat

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
          ("photoId" -> photo.id.is) ~
          ("hasGpsInfo" -> photo.hasGpsInfo.is)
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
      def extractI(key : String,func : Int => Any){
        (body \ key).toOpt match{
          case Some(JString(v)) => func(v.toInt)
          case Some(JInt(v)) => func(v.toInt)
          case Some(JDouble(v)) => func(v.toInt)
          case _ => //nothing
        }
      }

      extractI("goodness", v => photo.goodness := v)
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
    case Req("api" :: "alert" :: Nil, _ , GetRequest) => {
      val photos = Photo.findBadPhotos()
      photoListToJSON(photos)
    }

    case Req("api" :: "photo" :: "calendar" :: Nil, _ , GetRequest) => {

      val year = S.param("year").map(_.toInt).openOr{
        Calendar.getInstance().get(Calendar.YEAR)
      }
      val month = S.param("month").map(_.toInt).openOr{
        Calendar.getInstance().get(Calendar.MONTH) + 1
      }

      val photos = Photo.findInMonth(year,month)
      logger.debug("Find " + photos.length + " photos at " + year + "/" + month)
      val f = new SimpleDateFormat("MM/dd")
      val grouped = photos.groupBy(photo => f.format(photo.uploaded.is))

      val c = Calendar.getInstance();
      c.set(year,month - 1,1)

      val base = ("year" -> year) ~
      ("month" -> month) ~
      ("firstDayOfWeek" -> c.get(Calendar.DAY_OF_WEEK))

      grouped.foldLeft(base)( (s,e) => {
        s ~
        (e._1 -> photoListToJSON(e._2))
      })


    }

  }


  implicit def dateToJValue(d : Date) = JString(d.getTime.toString)

  def photoListToJSON(photos : List[Photo]) : JValue = {
    photos.map(photo => {
      ("id" -> photo.id.is) ~
      ("resourceKey" ->  photo.resourceKey.is) ~
        ("ownerId" -> photo.user.is) ~
        ("ownerName" -> photo.user.obj.open_!.nickname.is) ~
        ("comment" -> photo.comment.is) ~
        ("captured" -> photo.captured.is.getTime)  ~
        ("hasGps" -> photo.hasGpsInfo.is)  ~
        ("latitude" -> photo.latitude.is) ~
        ("longitude" -> photo.longitude.is) ~
        ("goodness" -> photo.goodness.is)
    })
  }


}
