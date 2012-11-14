package jp.utokyo.photogather.util

import java.security.MessageDigest

/**
 * Created with IntelliJ IDEA.
 * User: takezoux3
 * Date: 12/11/14
 * Time: 23:54
 * To change this template use File | Settings | File Templates.
 */
object EncryptUtil {


  def sha1Digest(data : Array[Byte]) = {
    val md = MessageDigest.getInstance("SHA-1");
    md.digest(data).map( b => "%x".format(b)).mkString
  }

}
