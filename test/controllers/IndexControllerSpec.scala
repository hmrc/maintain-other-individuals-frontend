/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import base.SpecBase
import connectors.TrustConnector
import models.TaskStatus.InProgress
import models.{Name, OtherIndividual, OtherIndividuals, TrustDetails}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  "Index Controller" must {

    "redirect to AddAnOtherIndividualController and set task to in progress" in {

      val identifier = "1234567890"
      val startDate = "2019-06-01"
      val isTaxable = true
      val isUnderlyingData5mld = false

      val mockTrustConnector = mock[TrustConnector]
      val mockTrustsStoreService = mock[TrustsStoreService]

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      when(mockTrustConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(startDate = LocalDate.parse(startDate), trustTaxable = Some(isTaxable))))

      when(mockTrustsStoreService.updateTaskStatus(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      when(mockTrustConnector.isTrust5mld(any())(any(), any()))
        .thenReturn(Future.successful(isUnderlyingData5mld))

      when(mockTrustConnector.getOtherIndividuals(any())(any(), any()))
        .thenReturn(Future.successful(
          OtherIndividuals(
            List(OtherIndividual(Name("Adam", None, "Test"), None, None, None, None, None, None, LocalDate.now, provisional = false))
          )
        ))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector),
          bind[TrustsStoreService].toInstance(mockTrustsStoreService)
        ).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(identifier).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.AddAnOtherIndividualController.onPageLoad().url)

      verify(mockTrustsStoreService).updateTaskStatus(eqTo(identifier), eqTo(InProgress))(any(), any())

      application.stop()
    }
  }
}
