/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.individual.remove

import base.SpecBase
import connectors.TrustConnector
import forms.DateRemovedFromTrustFormProvider
import models.{Name, NationalInsuranceNumber, OtherIndividual, OtherIndividuals}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{TrustService, TrustServiceImpl}
import uk.gov.hmrc.http.HttpResponse
import views.html.individual.remove.WhenRemovedView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class WhenRemovedControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new DateRemovedFromTrustFormProvider()

  private def form = formProvider.withPrefixAndEntityStartDate("otherIndividual.whenRemoved", LocalDate.now())

  val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)

  val index = 0

  val name = "Name 1"
  val mockConnector: TrustConnector = mock[TrustConnector]

  val fakeService = new TrustServiceImpl(mockConnector)

  lazy val dateRemovedFromTrustRoute: String = routes.WhenRemovedController.onPageLoad(index).url

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateRemovedFromTrustRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateRemovedFromTrustRoute)
      .withFormUrlEncodedBody(
        "value.day" -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year" -> validAnswer.getYear.toString
      )

  def otherIndividual(id: Int): OtherIndividual = OtherIndividual(
    name = Name(firstName = "Name", middleName = None, lastName = s"$id"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    countryOfNationality = None,
    countryOfResidence = None,
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    mentalCapacityYesNo = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = false
  )

  val otherIndividuals: List[OtherIndividual] = List(otherIndividual(1), otherIndividual(2), otherIndividual(3))

  "WhenRemoved Controller" must {

    "return OK and the correct view for a GET" in {

      when(mockConnector.getOtherIndividuals(any())(any(), any()))
        .thenReturn(Future.successful(OtherIndividuals(otherIndividuals)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[TrustConnector].toInstance(mockConnector)).build()

      val result = route(application, getRequest()).value

      val view = application.injector.instanceOf[WhenRemovedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, index, name)(getRequest(), messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockConnector.removeOtherIndividual(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[TrustService].toInstance(fakeService)
          )
          .build()

      val result = route(application, postRequest()).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.AddAnOtherIndividualController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[TrustConnector].toInstance(mockConnector)).build()

      val request =
        FakeRequest(POST, dateRemovedFromTrustRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[WhenRemovedView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, name)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, getRequest()).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, postRequest()).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
