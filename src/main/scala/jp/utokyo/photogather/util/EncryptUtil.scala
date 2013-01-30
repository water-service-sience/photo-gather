package jp.utokyo.photogather.util

import java.security.MessageDigest
import util.Random

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


  val random = new Random
  val AlphabetAndNumbers = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toArray

  def randomString(length : Int) : String = {
    randomString(length,AlphabetAndNumbers)
  }

  def randomString(length : Int,characterList : Array[Char]) : String = {
    val b = new StringBuilder(length)
    val max = characterList.length
    for (i <- 0 until length){
      b.append(characterList(random.nextInt(max)))
    }
    b.toString()
  }
}
