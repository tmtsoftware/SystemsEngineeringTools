package org.tmt.setools

import java.io.File

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.{FunSuite, Matchers}

class DocushareTest extends FunSuite with Matchers {

  val docushare = new DocushareAccess()

  test("should login and get doc") {
    val user = sys.env.getOrElse("DCCUSER", "")
    val pw = sys.env.getOrElse("DCCPW", "")
    val uri = "https://docushare.tmt.org/docushare/dsweb/Get/Document-78705/C1_M2.csv"

    val expectedOutput =
      "T(h) , Tx(um) , Ty(um) , Tz(um) , Rx(mas) , Ry(mas) , Rz(mas) \n" +
        "2 , 4.054848 , 2.536640 , 15.368726 , 457.689464 , -67.663675 , -21.071520 \n" +
        "4 , 2.544668 , 3.001592 , 60.801415 , 215.696022 , -17.657840 , -68.255433 \n" +
        "6 , 5.380159 , 7.383948 , 150.549557 , 152.171938 , 55.774434 , -116.474102 \n" +
        "8 , 7.794927 , 12.107913 , 318.427720 , 113.190533 , 173.034608 , -167.729455 \n" +
        "10 , 8.768304 , 19.269950 , 540.455876 , 64.382494 , 311.429398 , -211.402552 \n" +
        "12 , 13.913246 , 32.563175 , 758.383190 , 114.024414 , 439.255442 , -251.829137 \n"


    docushare.login(user, pw) match {
      case Some(cookies) =>
        cookies.size shouldBe 2
        docushare.get(uri, cookies) shouldBe expectedOutput
      case _ => fail
    }
  }

  test("login") {
    docushare.login()
  }

  test("should login and publish file") {
    val user = sys.env.getOrElse("DCCUSER", "")
    val pw = sys.env.getOrElse("DCCPW", "")
    val collection = 24009
    val file = new File("/Users/Weiss/Desktop/testReportFile5.tsv")
    val packageName = "CSW"
    val version = 0.7f
    val author = "Jason Weiss"
    val testNumber="1234"

    docushare.login(user, pw) match {
      case Some(cookies) =>
        cookies.size shouldBe 2
        docushare.publish(cookies, collection, file, packageName, version, author, testNumber) shouldBe StatusCodes.OK
      case _ => fail
    }

  }

  test("should login and publish file manual cookies") {
    val collection = 24009
    val file = new File("/Users/Weiss/Desktop/testReportFile5.tsv")
    val packageName = "CSW"
    val version = 0.7f
    val author = "Jason Weiss"
    val testNumber="1234"
    val cookies = Seq()

    docushare.publish(cookies, collection, file, packageName, version, author, testNumber) shouldBe StatusCodes.OK

  }

}
