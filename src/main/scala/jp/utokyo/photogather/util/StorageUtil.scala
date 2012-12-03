package jp.utokyo.photogather.util

import java.io.{FileInputStream, File, FileOutputStream}
import org.slf4j.LoggerFactory
import net.liftweb.util.Props

/**
 * 
 * User: takeshita
 * DateTime: 12/11/13 2:44
 */
object StorageUtil extends FileStorage{
  val logger = LoggerFactory.getLogger(getClass)

  lazy val storage : FileStorage = new LocalFileStorage(Props.get("photo.dir").getOrElse( "./data/uploaded/photo"))

  def save(filename: String, data: Array[Byte]) = {
    logger.debug("Save file : " + filename)
    storage.save(filename,data)
  }

  def load(filename: String) = storage.load(filename)


}


trait FileStorage{
  def save(filename : String , data : Array[Byte]) : Any
  def load(filename : String) : Array[Byte]
}

class LocalFileStorage(dir : String) extends FileStorage {

  {
    val d = new File(dir)
    if (!d.exists()){
      d.mkdirs()
    }
  }


  def save(filename: String, data: Array[Byte]) = {
    val output = new FileOutputStream(new File(dir,filename))
    try{
      output.write(data)
    }finally{
      output.close()
    }

  }

  def load(filename: String) = {
    val input = new FileInputStream(new File(dir,filename))
    val d = new Array[Byte](input.available())
    try{
      input.read(d)
      d
    }finally{
      input.close()
    }
  }
}