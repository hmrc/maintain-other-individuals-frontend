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

package controllers.individual.add

import base.SpecBase
import forms.DateAddedToTrustFormProvider
import models.{Name, NormalMode, UserAnswers}
import navigation.Navigator
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.{DateOfBirthPage, NamePage, WhenIndividualAddedPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.individual.add.WhenIndividualAddedView

import java.time.{LocalDate, ZoneOffset}

class WhenIndividualAddedControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new DateAddedToTrustFormProvider()
  private val date: LocalDate = LocalDate.parse("2019-02-03")

  private def form: Form[LocalDate] = formProvider.withConfig("otherIndividual.whenIndividualAdded", date)

  private val validAnswer = LocalDate.now(ZoneOffset.UTC)

  private lazy val addedDateRoute = routes.WhenIndividualAddedController.onPageLoad(NormalMode).url

  private val name = Name("New", None, "Individual")

  override val emptyUserAnswers: UserAnswers =
    UserAnswers("id", "UTRUTRUTR", "sessionId", "id-UTRUTRUTR-sessionId", date)
      .set(NamePage, name)
      .success.value

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, addedDateRoute)

  private def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, addedDateRoute)
      .withFormUrlEncodedBody(
        "value.day" -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year" -> validAnswer.getYear.toString
      )

  "Other Individual added Date Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val result = route(application, getRequest()).value

      val view = application.injector.instanceOf[WhenIndividualAddedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, name.displayName)(getRequest(), messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(WhenIndividualAddedPage, validAnswer).success.value
        .set(NamePage, name).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val view = application.injector.instanceOf[WhenIndividualAddedView]

      val result = route(application, getRequest()).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), NormalMode, name.displayName)(getRequest(), messages).toString

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, addedDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[WhenIndividualAddedView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, name.displayName)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors" when {

      def userAnswers(trustStartDate: LocalDate, dateOfBirth: LocalDate) =
        UserAnswers("id", "UTRUTRUTR", "sessionId", "id-UTRUTRUTR-sessionId", trustStartDate)
          .set(NamePage, name).success.value
          .set(DateOfBirthPage, dateOfBirth).success.value

      def request(submittedDate: LocalDate) = FakeRequest(POST, addedDateRoute)
        .withFormUrlEncodedBody(
          "value.day" -> submittedDate.getDayOfMonth.toString,
          "value.month" -> submittedDate.getMonthValue.toString,
          "value.year" -> submittedDate.getYear.toString
        )

      def boundForm(submittedDate: LocalDate) = form.bind(Map(
        "value.day" -> submittedDate.getDayOfMonth.toString,
        "value.month" -> submittedDate.getMonthValue.toString,
        "value.year" -> submittedDate.getYear.toString
      ))

      "submitted date is before date of birth but after trust start date" in {

        val trustStartDate = LocalDate.parse("2017-02-03")
        val dateOfBirth = LocalDate.parse("2019-02-03")
        val submittedDate = LocalDate.parse("2018-02-03")

        val application = applicationBuilder(userAnswers = Some(userAnswers(trustStartDate, dateOfBirth))).build()

        val view = application.injector.instanceOf[WhenIndividualAddedView]

        val result = route(application, request(submittedDate)).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm(submittedDate), NormalMode, name.displayName)(getRequest(), messages).toString

        application.stop()
      }

      "submitted date is after date of birth but before trust start date" in {

        val trustStartDate = LocalDate.parse("2019-02-03")
        val dateOfBirth = LocalDate.parse("2017-02-03")
        val submittedDate = LocalDate.parse("2018-02-03")

        val application = applicationBuilder(userAnswers = Some(userAnswers(trustStartDate, dateOfBirth))).build()

        val view = application.injector.instanceOf[WhenIndividualAddedView]

        val result = route(application, request(submittedDate)).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm(submittedDate), NormalMode, name.displayName)(getRequest(), messages).toString

        application.stop()
      }

      "submitted date is before date of birth and before trust start date" in {

        val trustStartDate = LocalDate.parse("2019-02-03")
        val dateOfBirth = LocalDate.parse("2018-02-03")
        val submittedDate = LocalDate.parse("2017-02-03")

        val application = applicationBuilder(userAnswers = Some(userAnswers(trustStartDate, dateOfBirth))).build()

        val view = application.injector.instanceOf[WhenIndividualAddedView]

        val result = route(application, request(submittedDate)).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm(submittedDate), NormalMode, name.displayName)(getRequest(), messages).toString

        application.stop()
      }

    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, getRequest()).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(WhenIndividualAddedPage, validAnswer).success.value
        .set(NamePage, name).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[Navigator].toInstance(fakeNavigator))
          .build()

      val request =
        FakeRequest(POST, addedDateRoute)
          .withFormUrlEncodedBody(
            "value.day" -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year" -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

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
