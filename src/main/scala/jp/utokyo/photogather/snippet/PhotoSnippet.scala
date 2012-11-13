package jp.utokyo.photogather.snippet

import net.liftweb.http.{S, SHtml, StatefulSnippet}
import xml.{Text, NodeSeq}
import net.liftweb.util.Helpers
import Helpers._
import SHtml._
import jp.utokyo.photogather.util.StorageUtil
import jp.utokyo.photogather.model.{User, Photo}
import java.util.Date

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
        StorageUtil.save(f.fileName,f.file)

        val photo = Photo.create
        photo.user(User.currentUser.get)
        photo.resourceKey := f.fileName

        photo.save()

      }),
      "submit" -> submit("アップロード",() => {
        S.redirectTo("/photo/list")
      })
    )

  }

  def list( n : NodeSeq) : NodeSeq = {
    User.currentUser.get.photos.all.flatMap( photo => {
      bind("e",n,
        "filename" -> photo.resourceKey.is,
        "link" -> ((n:NodeSeq) => {
        <a href={"photo_edit?photoId=" + photo.id.is}>{n}</a>
        })
      )
    })
  }

  def edit( n : NodeSeq) : NodeSeq  = {


    val photo = Photo.find(S.param("photoId").open_!).open_!
    if(photo.user.is != User.currentUser.get.id.is){
      throw new Exception("Not own photo")
    }

    bind("e",n,
      "longitude" -> hidden(l => photo.longitude(l.toDouble),
        if(photo.hasGpsInfo.is) photo.longitude.is.toString else "", "id" -> "longitude"),
      "latitude" -> hidden(l => photo.latitude(l.toDouble),
        if(photo.hasGpsInfo.is) photo.latitude.is.toString else "", "id" -> "latitude"),
      "comment" -> textarea(photo.comment.is,photo.comment(_)),
      "submit" -> submit("変更",() => {
        photo.hasGpsInfo(true)
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
