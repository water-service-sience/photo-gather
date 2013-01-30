package jp.utokyo.photogather.stateless

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import jp.utokyo.photogather.model.User
import net.liftweb.common.Full
import net.liftweb.json.JsonDSL._
import jp.utokyo.photogather.function.PhotoFunction

/**
 * 
 * User: takeshita
 * DateTime: 12/11/13 1:58
 */
object LoginHandler extends RestHelper  {

  serve {
    case Req("login" :: "with" :: "accessKey" :: Nil ,_,GetRequest) => {

      val user = User.findByAccessKey(S.param("accessKey").open_!)
      User.logUserIn(user.open_!)
      S.redirectTo("/index")

    }
    case Req("logout" :: Nil ,_,GetRequest) => {
      User.logout()
      S.redirectTo("/sign_in")


    }
  }

}
