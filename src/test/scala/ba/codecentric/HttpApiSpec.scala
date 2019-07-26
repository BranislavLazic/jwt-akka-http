/*
 * Copyright (c) 2017 Branislav Lazic
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ba.codecentric

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ HttpHeader, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ Matchers, WordSpec }

class HttpApiSpec extends WordSpec with Matchers with ScalatestRouteTest {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import HttpApi._

  "HttpApi" should {
    "return 403 Unauthorized upon GET / " in {
      Get() ~> routes ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "return 403 Unauthorized when credentials are incorrect" in {
      Post("/", LoginRequest("admin", "something")) ~> routes ~> check {
        header(AccessTokenHeaderName) shouldBe Some(_: HttpHeader)
        status shouldBe StatusCodes.Unauthorized
      }
    }

    "return JWT token upon POST /" in {
      Post("/", LoginRequest("admin", "admin")) ~> routes ~> check {
        header(AccessTokenHeaderName) shouldBe Some(_: HttpHeader)
        status shouldBe StatusCodes.OK
      }
    }

    "access secured content after providing a correct JWT upon GET /" in {
      Post("/", LoginRequest("admin", "admin")) ~> routes ~> check {
        header(AccessTokenHeaderName).map { accessTokenHeader =>
          Get("/").addHeader(RawHeader("Authorization", accessTokenHeader.value())) ~> routes ~> check {
            status shouldBe StatusCodes.OK
          }
        }
      }
    }
  }
}
