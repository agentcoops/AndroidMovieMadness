import sbt._

trait Defaults {
  def androidPlatformName = "android-4"
}
class MovieMadness(info: ProjectInfo) extends ParentProject(info) {
  override def shouldCheckOutputDirectories = false
  override def updateAction = task { None }

  lazy val main  = project(".", "MovieMadness", new MainProject(_))
  lazy val tests = project("tests",  "tests", new TestProject(_), main)

  class MainProject(info: ProjectInfo) extends AndroidProject(info) with Defaults with MarketPublish {
    val keyalias  = "change-me"
    val scalatest = "org.scalatest" % "scalatest" % "1.0" % "test"
    val dispatch = "net.databinder" % "dispatch-http_2.7.7" % "0.7.4"
    val httpclient = "org.apache.httpcomponents" % "httpclient" % "4.0.1"
    val jcip = "net.jcip" % "jcip-annotations" % "1.0" % "provided->default"
    val lag_net = "lag.net repository" at "http://www.lag.net/repo"
    val configgy_test = "net.lag" % "configgy" % "1.4" % "test->default"
  }

  class TestProject(info: ProjectInfo) extends AndroidTestProject(info) with Defaults
}
