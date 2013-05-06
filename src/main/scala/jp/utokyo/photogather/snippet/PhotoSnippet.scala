package jp.utokyo.photogather.snippet

import net.liftweb.http.{S, SHtml, StatefulSnippet}
import xml.{Text, NodeSeq}
import net.liftweb.util.Helpers
import Helpers._
import SHtml._
import jp.utokyo.photogather.util.{PhotoUtil, EncryptUtil, StorageUtil}
import jp.utokyo.photogather.model.{User, Photo}
import java.util.Date
import java.io.File
import jp.utokyo.photogather.stateless.JsonHandler
import java.text.SimpleDateFormat
import jp.utokyo.photogather.function.PhotoFunction

/**
 * 
 * User: takeshita
 * DateTime: 12/11/13 2:43
 */
class PhotoSnippet extends StatefulSnippet {
  def dispatch: DispatchIt = {
    case "upload" => upload _
    case "list" => list _
    case "edit" => edit _
  }



  def upload(n : NodeSeq) : NodeSeq  = {


    bind("e",n,
      "file" -> fileUpload( f => {

        val photo = PhotoFunction.saveToStorage(User.currentUser, f.fileName, f.file)

      }),
      "submit" -> submit("アップロード",() => {
        S.redirectTo("/photo/upload_list")
      })
    )

  }

  def list( n : NodeSeq) : NodeSeq = {
    val format = new SimpleDateFormat("yy/MM/dd HH:mm")
    User.currentUser.photos.all.flatMap( photo => {
      bind("e",n,
        "image" -> <img src={JsonHandler.toImageUrl(photo.resourceKey.is)} class="small-image"></img>,
        "place" -> photo.comment.is,
        "id" -> photo.id,
        "captured" -> format.format(photo.captured.is),
        "uploaded" -> format.format(photo.uploaded.is),
        "link" -> ((n:NodeSeq) => {
        <a href={"photo_edit?photoId=" + photo.id.is}>{n}</a>
        })
      )
    })
  }

  def edit( n : NodeSeq) : NodeSeq  = {


    val photo = Photo.find(S.param("photoId").open_!).open_!
    if(photo.user.is != User.currentUser.id.is){
      throw new Exception("Not own photo")
    }

    def safeToDouble(v : String) = try{
      v.toDouble
    }catch{
      case e => 0.0
    }

    bind("e",n,
      "image" -> <img src={"/images/uploaded/" + photo.resourceKey.is} class="large-image" />,
      "longitude" -> hidden(l => photo.longitude(safeToDouble(l)),
        if(photo.hasGpsInfo.is) photo.longitude.is.toString else "", "id" -> "longitude"),
      "latitude" -> hidden(l => photo.latitude(safeToDouble(l)),
        if(photo.hasGpsInfo.is) photo.latitude.is.toString else "", "id" -> "latitude"),
      "comment" -> textarea(photo.comment.is,photo.comment(_),"id" -> "photo-comment"),
      "submit" -> submit("変更",() => {
        photo.hasGpsInfo(photo.longitude.is != 0 && photo.latitude.is != 0)
        photo.save()
        val req = S.request.open_!
        S.redirectTo(req.uri  + "?" + req.request.queryString.open_!)
      })
    )

  }

  def uploadedPhotos(n : NodeSeq) : NodeSeq = {
    val photos = Photo.findAll();

    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._

    bind("e",n,
    "data" -> scala.xml.Text(compact(render(JArray(
      photos.map(p => {
        ("latitucde" -> p.latitude.is) ~
        ("longitude" -> p.longitude.is) ~
        ("img" -> "https://www.google.co.jp/intl/ja_ALL/images/logos/images_logo_lg.gif")
      })
    )))))



  }


}
