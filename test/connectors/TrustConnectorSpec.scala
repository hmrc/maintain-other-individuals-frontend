/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.{Name, OtherIndividual, OtherIndividuals, RemoveOtherIndividual, TrustDetails, YesNoDontKnow}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class TrustConnectorSpec
    extends SpecBase
    with Generators
    with ScalaFutures
    with Inside
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with IntegrationPatience {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  val identifier = "0987654321"
  val index      = 0

  private def amendOtherIndividualUrl(identifier: String, index: Int) =
    s"/trusts/other-individuals/amend/$identifier/$index"

  private def addOtherIndividualUrl(identifier: String) = s"/trusts/other-individuals/add/$identifier"

  private def isTrust5mldUrl(identifier: String) = s"/trusts/$identifier/is-trust-5mld"

  private def removeOtherIndividualUrl(identifier: String) = s"/trusts/other-individuals/$identifier/remove"

  "trust connector" when {

    "get trusts details" in {

      val json = Json.parse("""
          |{
          | "startDate": "1920-03-28",
          | "lawCountry": "AD",
          | "administrationCountry": "GB",
          | "residentialStatus": {
          |   "uk": {
          |     "scottishLaw": false,
          |     "preOffShore": "AD"
          |   }
          | },
          | "typeOfTrust": "Will Trust or Intestacy Trust",
          | "deedOfVariation": "Previously there was only an absolute interest under the will",
          | "interVivos": false
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled"                  -> false
          ): _*
        )
        .build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(s"/trusts/trust-details/$identifier/transformed"))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustDetails(identifier)

      whenReady(processed) { r =>
        r mustBe TrustDetails(startDate = LocalDate.parse("1920-03-28"), None)
      }
    }

    "get other individuals returns a trust with empty lists" must {

      "return a default empty list of other individuals" in {

        val json = Json.parse("""
            |{
            | "naturalPerson": [
            | ]
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled"                  -> false
            ): _*
          )
          .build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/other-individuals/$identifier/transformed"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getOtherIndividuals(identifier)

        whenReady(processed) { result =>
          result mustBe OtherIndividuals(otherIndividuals = Nil)
        }

        application.stop()
      }

    }

    "get other individuals" must {

      "parse the response and return the other individuals" in {

        val json = Json.parse("""
            |{
            | "naturalPerson" : [
            |     {
            |       "lineNo" : "79",
            |       "name" : {
            |         "firstName" : "Other",
            |         "lastName" : "Individual"
            |       },
            |       "entityStart" : "2019-09-23"
            |     }
            |   ]
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled"                  -> false
            ): _*
          )
          .build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/other-individuals/$identifier/transformed"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getOtherIndividuals(identifier)

        whenReady(processed) { result =>
          result mustBe
            OtherIndividuals(otherIndividuals =
              List(
                OtherIndividual(
                  name = Name("Other", None, "Individual"),
                  dateOfBirth = None,
                  countryOfNationality = None,
                  countryOfResidence = None,
                  identification = None,
                  address = None,
                  mentalCapacityYesNo = Some(YesNoDontKnow.DontKnow),
                  entityStart = LocalDate.parse("2019-09-23"),
                  provisional = false
                )
              )
            )
        }

        application.stop()
      }
    }

    "amending an individual" must {

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled"                  -> false
            ): _*
          )
          .build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendOtherIndividualUrl(identifier, index)))
            .willReturn(ok)
        )

        val individual = OtherIndividual(
          name = Name(
            firstName = "First",
            middleName = None,
            lastName = "Last"
          ),
          dateOfBirth = None,
          countryOfNationality = None,
          countryOfResidence = None,
          identification = None,
          address = None,
          mentalCapacityYesNo = None,
          entityStart = LocalDate.parse("2020-03-27"),
          provisional = false
        )

        val result = connector.amendOtherIndividual(identifier, index, individual)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled"                  -> false
            ): _*
          )
          .build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendOtherIndividualUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val individual = OtherIndividual(
          name = Name(
            firstName = "First",
            middleName = None,
            lastName = "Last"
          ),
          dateOfBirth = None,
          countryOfNationality = None,
          countryOfResidence = None,
          identification = None,
          address = None,
          mentalCapacityYesNo = None,
          entityStart = LocalDate.parse("2020-03-27"),
          provisional = false
        )

        val result = connector.amendOtherIndividual(identifier, index, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "isTrust5mld" must {

      "return true" when {
        "untransformed data is 5mld" in {

          val json = JsBoolean(true)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled"                  -> false
              ): _*
            )
            .build()

          val connector = application.injector.instanceOf[TrustConnector]

          server.stubFor(
            get(urlEqualTo(isTrust5mldUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.isTrust5mld(identifier)

          whenReady(processed) { r =>
            r mustBe true
          }
        }
      }

      "return false" when {
        "untransformed data is 4mld" in {

          val json = JsBoolean(false)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled"                  -> false
              ): _*
            )
            .build()

          val connector = application.injector.instanceOf[TrustConnector]

          server.stubFor(
            get(urlEqualTo(isTrust5mldUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.isTrust5mld(identifier)

          whenReady(processed) { r =>
            r mustBe false
          }
        }
      }
    }

  }

  "adding an individual" must {

    "Return OK when the request is successful for add" in {

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled"                  -> false
          ): _*
        )
        .build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        post(urlEqualTo(addOtherIndividualUrl(identifier)))
          .willReturn(ok)
      )

      val individual = OtherIndividual(
        name = Name(
          firstName = "First",
          middleName = None,
          lastName = "Last"
        ),
        dateOfBirth = None,
        countryOfNationality = None,
        countryOfResidence = None,
        identification = None,
        address = None,
        mentalCapacityYesNo = None,
        entityStart = LocalDate.parse("2020-03-27"),
        provisional = false
      )

      val result = connector.addOtherIndividual(identifier, individual)

      result.futureValue.status mustBe OK

      application.stop()
    }
  }

  "removing an individual" must {

    "Return OK when the request is successful for remove" in {

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled"                  -> false
          ): _*
        )
        .build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        put(urlEqualTo(removeOtherIndividualUrl(identifier)))
          .willReturn(ok)
      )

      val individual = RemoveOtherIndividual(
        index = 0,
        endDate = LocalDate.parse("2020-03-27")
      )

      val result = connector.removeOtherIndividual(identifier, individual)

      result.futureValue.status mustBe OK

      application.stop()
    }
  }

}
