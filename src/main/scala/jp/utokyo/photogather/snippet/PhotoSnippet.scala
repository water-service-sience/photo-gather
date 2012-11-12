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
        "filename" -> photo.resourceKey.is
      )
    })
  }


}
