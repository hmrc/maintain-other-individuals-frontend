/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import base.SpecBase
import connectors.TrustConnector
import forms.YesNoFormProvider
import models.{Name, NationalInsuranceNumber, OtherIndividual, OtherIndividuals}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.individual.RemoveYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import views.html.individual.remove.RemoveOtherIndividualView

import scala.concurrent.Future

class RemoveOtherIndividualControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ScalaFutures {

  val messagesPrefix = "removeOtherIndividual"

  lazy val formProvider = new YesNoFormProvider()
  lazy val form = formProvider.withPrefix(messagesPrefix)

  lazy val name : String = "Name 1"

  val mockConnector: TrustConnector = mock[TrustConnector]

  def otherIndividual(id: Int, provisional : Boolean) = OtherIndividual(
    name = Name(firstName = "Name", middleName = None, lastName = s"$id"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = provisional
  )

  val expectedResult = otherIndividual(2, provisional = true)

  val otherIndividuals = List(
    otherIndividual(1, provisional = false),
    expectedResult,
    otherIndividual(3, provisional = true)
  )

  "RemoveOtherIndividual Controller" when {

    "return OK and the correct view for a GET" in {

      val index = 0

      implicit val hc : HeaderCarrier = HeaderCarrier()

      when(mockConnector.getOtherIndividuals(any())(any(), any()))
        .thenReturn(Future.successful(OtherIndividuals(otherIndividuals)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveOtherIndividualController.onPageLoad(index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveOtherIndividualView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, index, name)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(RemoveYesNoPage, true).success.value

      when(mockConnector.getOtherIndividuals(any())(any(), any()))
        .thenReturn(Future.successful(OtherIndividuals(otherIndividuals)))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(bind[TrustConnector].toInstance(mockConnector)).build()

      val request = FakeRequest(GET, routes.RemoveOtherIndividualController.onPageLoad(0).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveOtherIndividualView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), 0, name)(fakeRequest, messages).toString

      application.stop()
    }

    "not removing the otherIndividual" must {

      "redirect to the add to page when valid data is submitted" in {

        val index = 0

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveOtherIndividualController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddAnOtherIndividualController.onPageLoad().url

        application.stop()
      }
    }

    "removing an existing otherIndividual" must {

      "redirect to the next page when valid data is submitted" in {

        val index = 0

        when(mockConnector.getOtherIndividuals(any())(any(), any()))
          .thenReturn(Future.successful(OtherIndividuals(otherIndividuals)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveOtherIndividualController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.individual.remove.routes.WhenRemovedController.onPageLoad(0).url

        application.stop()
      }
    }

    "removing a new otherIndividual" must {

      "redirect to the add to page, removing the otherIndividual" in {

        val index = 2

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getOtherIndividuals(any())(any(), any()))
          .thenReturn(Future.successful(OtherIndividuals(otherIndividuals)))

        when(mockConnector.removeOtherIndividual(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200)))

        val request =
          FakeRequest(POST, routes.RemoveOtherIndividualController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddAnOtherIndividualController.onPageLoad().url

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val index = 0

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[TrustConnector].toInstance(mockConnector)).build()

      val request =
        FakeRequest(POST, routes.RemoveOtherIndividualController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemoveOtherIndividualView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, name)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.RemoveOtherIndividualController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, routes.RemoveOtherIndividualController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
