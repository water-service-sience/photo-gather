package jp.utokyo.photogather.util

import org.specs2.mutable.SpecificationWithJUnit
import java.io.FileInputStream

/**
 * Created with IntelliJ IDEA.
 * User: takezou
 * Date: 12/11/25
 * Time: 20:59
 * To change this template use File | Settings | File Templates.
 */
class PhotoUtilTest extends SpecificationWithJUnit {

  "get gps" should {
    "get gps in jpeg" in {
      val data = loadFile("test.jpg")

      val gps = PhotoUtil.extractGpsInfo(data)
      println(gps)

      gps must beSome[(Double,Double)]

      gps.get._1 must beCloseTo(35.683349,0.001)
      gps.get._2 must beCloseTo(139.7666717,0.001)

    }
  }

  def loadFile(filename : String) = {
    val s = getClass.getClassLoader.getResourceAsStream(filename)//new FileInputStream(filename)
    val b = new Array[Byte](s.available())
    s.read(b)
    s.close()

    b
  }

}
