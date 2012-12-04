package jp.utokyo.photogather.stateless

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import jp.utokyo.photogather.util.StorageUtil
import jp.utokyo.photogather.model.User
import net.liftweb.common.Full
import net.liftweb.common.Full
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import jp.utokyo.photogather.function.PhotoFunction

/**
 * 
 * User: takeshita
 * DateTime: 12/11/13 1:58
 */
object APIHandler extends RestHelper  {

  serve {
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
    case "api" :: "upload" :: Nil JsonGet _ => {
      val accessKey = S.param("accessKey").open_!
      val file = S.request.open_!.uploadedFiles(0)

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
  }

}
