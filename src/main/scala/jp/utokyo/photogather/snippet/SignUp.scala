package jp.utokyo.photogather.snippet

import jp.utokyo.photogather.model.User
import net.liftweb.http.{S, SHtml, StatefulSnippet}
import xml.{Text, NodeSeq}
import SHtml._
import net.liftweb.util.Helpers
import Helpers._
import net.liftweb.common.Full
import net.liftweb.mapper.By
import java.util.Date

/**
 *
 * Login: takeshita
 * Create: 12/02/19 21:57
 */

class SignUp extends StatefulSnippet with QuerySupport {
  def dispatch: SignUp#DispatchIt = {
    case "signIn" => signIn _
    case "signUp" => signUp _
    case "currentUser" => currentUser _
  }
  
  def currentUser(n : NodeSeq) : NodeSeq = {
    if(!User.loggedIn_?){
      Text("未ログイン")
    }else{
      bind("e",n,
        "id" -> User.currentUser.id.is.toString,
        "name" -> User.currentUser.nickname
      )
    }
  }

  def signIn(n : NodeSeq) : NodeSeq = {

    val pageFrom = S.param("from").openOr("/index.html")


    bind("e",n,
    "submit" -> SHtml.submit ("ログイン", () => {
      User.find(By(User.username,paramAsString("username"))).map( u => {


        if(u.password == (paramAsString("password") )){
          User.logUserIn(u)
          S.redirectTo(pageFrom)
        }else{
          S.warning("パスワードが違います。")
        }
        "ok"
      }).openOr({
        S.warning("ユーザーが存在しません")
        "ng"
      })
    }))

  }
  def signUp(n : NodeSeq) : NodeSeq = {
    
    bind("e",n,
    "submit" -> SHtml.submit("作成" , () => {
      val p1 = paramAsString("password1")
      val p2 = paramAsString("password2")
      if(p1 != p2){
        throw new Exception("Input same password!")
      }
      val user = User.createUser(paramAsString("username"),p1)

      User.logUserIn(user)
      S.redirectTo("/index.html")
    }))

  }

}
