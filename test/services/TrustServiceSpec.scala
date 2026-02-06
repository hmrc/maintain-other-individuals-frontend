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

package services

import base.SpecBase
import connectors.TrustConnector
import models.{Name, NationalInsuranceNumber, OtherIndividual, OtherIndividuals}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TrustServiceSpec extends SpecBase {

  private val identifier: String = "utr"
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val individual: OtherIndividual = OtherIndividual(
    name = Name("Joe", None, "Bloggs"),
    dateOfBirth = None,
    countryOfNationality = None,
    identification = None,
    countryOfResidence = None,
    address = None,
    mentalCapacityYesNo = None,
    entityStart = LocalDate.parse("2000-01-01"),
    provisional = true
  )

  "TrustService" when {

    val mockConnector = mock[TrustConnector]

    val service = new TrustServiceImpl(mockConnector)

    ".getIndividualNinos" must {

      "return empty list" when {

        "no individuals" in {

          when(mockConnector.getOtherIndividuals(any())(any(), any()))
            .thenReturn(Future.successful(OtherIndividuals(Nil)))

          val result = Await.result(service.getIndividualNinos(identifier, None), Duration.Inf)

          result mustBe Nil
        }

        "there are individuals but they don't have a NINo" in {

          val individuals = List(
            individual.copy(identification = None)
          )

          when(mockConnector.getOtherIndividuals(any())(any(), any()))
            .thenReturn(Future.successful(OtherIndividuals(individuals)))

          val result = Await.result(service.getIndividualNinos(identifier, None), Duration.Inf)

          result mustBe Nil
        }

        "there is an individual with a NINo but it's the same index as the one we're amending" in {

          val individuals = List(
            individual.copy(identification = Some(NationalInsuranceNumber("nino")))
          )

          when(mockConnector.getOtherIndividuals(any())(any(), any()))
            .thenReturn(Future.successful(OtherIndividuals(individuals)))

          val result = Await.result(service.getIndividualNinos(identifier, Some(0)), Duration.Inf)

          result mustBe Nil
        }
      }

      "return NINos" when {

        "individuals have NINos and we're adding (i.e. no index)" in {

          val individuals = List(
            individual.copy(identification = Some(NationalInsuranceNumber("nino1"))),
            individual.copy(identification = Some(NationalInsuranceNumber("nino2")))
          )

          when(mockConnector.getOtherIndividuals(any())(any(), any()))
            .thenReturn(Future.successful(OtherIndividuals(individuals)))

          val result = Await.result(service.getIndividualNinos(identifier, None), Duration.Inf)

          result mustBe List("nino1", "nino2")
        }

        "individuals have NINos and we're amending" in {

          val individuals = List(
            individual.copy(identification = Some(NationalInsuranceNumber("nino1"))),
            individual.copy(identification = Some(NationalInsuranceNumber("nino2")))
          )

          when(mockConnector.getOtherIndividuals(any())(any(), any()))
            .thenReturn(Future.successful(OtherIndividuals(individuals)))

          val result = Await.result(service.getIndividualNinos(identifier, Some(0)), Duration.Inf)

          result mustBe List("nino2")
        }
      }
    }
  }

}
