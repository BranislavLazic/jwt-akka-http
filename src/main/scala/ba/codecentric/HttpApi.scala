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

import java.util.concurrent.TimeUnit

import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.{ Directive1, Route }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.pattern._

import scala.util.Failure

object HttpApi {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import authentikat.jwt._

  final val Name                  = "http-api"
  final val AccessTokenHeaderName = "X-Access-Token"

  final case class LoginRequest(username: String, password: String)

  private val tokenExpiryPeriodInDays = 1
  private val secretKey               = "super_secret_key"
  private val header                  = JwtHeader("HS256")

  private def login: Route = post {
    entity(as[LoginRequest]) {
      case lr @ LoginRequest("admin", "admin") =>
        val claims = setClaims(lr.username, tokenExpiryPeriodInDays)
        respondWithHeader(RawHeader(AccessTokenHeaderName, JsonWebToken(header, claims, secretKey))) {
          complete(StatusCodes.OK)
        }
      case LoginRequest(_, _) => complete(StatusCodes.Unauthorized)
    }
  }

  private def securedContent: Route = get {
    authenticated { claims =>
      complete(s"User: ${claims.getOrElse("user", "")} has accessed a secured content!")
    }
  }

  private def authenticated: Directive1[Map[String, Any]] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) if isTokenExpired(jwt) =>
        complete(StatusCodes.Unauthorized -> "Session expired.")

      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
        provide(getClaims(jwt))

      case _ => complete(StatusCodes.Unauthorized)
    }

  private def setClaims(username: String, expiryPeriodInDays: Long): JwtClaimsSetMap =
    JwtClaimsSet(
      Map("user" -> username,
          "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
            .toMillis(expiryPeriodInDays)))
    )

  private def getClaims(jwt: String): Map[String, String] = jwt match {
    case JsonWebToken(_, claims, _) => claims.asSimpleMap.getOrElse(Map.empty[String, String])
  }

  private def isTokenExpired(jwt: String): Boolean =
    getClaims(jwt).get("expiredAt").exists(_.toLong < System.currentTimeMillis())

  def routes: Route = login ~ securedContent

  def apply(host: String, port: Int) = Props(new HttpApi(host, port))
}

final class HttpApi(host: String, port: Int) extends Actor with ActorLogging {
  import HttpApi._
  import context.dispatcher

  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  Http(context.system).bindAndHandle(routes, host, port).pipeTo(self)

  override def receive: Receive = {
    case ServerBinding(address) =>
      log.info("Server successfully bound at {}:{}", address.getHostName, address.getPort)
    case Failure(cause) =>
      log.error("Failed to bind server", cause)
      context.system.terminate()
  }
}
