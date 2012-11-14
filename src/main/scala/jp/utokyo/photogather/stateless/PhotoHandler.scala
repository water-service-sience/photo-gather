package jp.utokyo.photogather.stateless

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JString
import net.liftweb.http.{InMemoryResponse, GetRequest, Req}
import jp.utokyo.photogather.util.StorageUtil

/**
 * 
 * User: takeshita
 * DateTime: 12/11/13 1:58
 */
object PhotoHandler extends RestHelper  {

  serve {
    case Req("images" :: "uploaded" :: key :: Nil, extension,GetRequest) => {

      val data = StorageUtil.load(key + "." + extension)

      new InMemoryResponse(data,Nil,Nil,200)

    }
  }

}
