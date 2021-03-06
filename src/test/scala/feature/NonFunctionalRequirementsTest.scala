package feature

import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import env.RunningTestEnvironment
import io.circe.optics.JsonPath._
import io.fintrospect.ContentTypes
import io.fintrospect.formats.Circe.JsonFormat
import org.scalatest.{FunSpec, Matchers}

import scala.xml.Utility.trim
import scala.xml.XML

class NonFunctionalRequirementsTest extends FunSpec with Matchers with RunningTestEnvironment {

  it("responds to ping") {
    val response = env.responseTo(Request("/internal/ping"))
    response.status shouldBe Ok
    response.content shouldBe "pong"
  }

  it("responds to uptime") {
    val response = env.responseTo(Request("/internal/uptime"))
    response.status shouldBe Ok
    response.content shouldBe "uptime is: 0s"
  }

  it("serves static content") {
    val response = env.responseTo(Request("/style.css"))
    response.status shouldBe Ok
    response.content.contains("font-family: \"Droid Sans\"") shouldBe true
  }

  it("has a sitemap") {
    val response = env.responseTo(Request("/sitemap.xml"))
    response.status shouldBe Ok
    response.contentType.startsWith(ContentTypes.APPLICATION_XML.value) shouldBe true
    val siteMap = trim(XML.loadString(response.content))
    ((siteMap \\ "urlset" \\ "url") (0) \\ "loc").text shouldBe "http://my.security.system/users"
    ((siteMap \\ "urlset" \\ "url") (1) \\ "loc").text shouldBe "http://my.security.system"
  }

  it("provides API documentation in swagger 2.0 format") {
    val response = env.responseTo(Request("/security/api-docs"))
    response.status shouldBe Ok

    root.swagger.string.getOption(JsonFormat.parse(response.content)) shouldBe Option("2.0")
  }
}
