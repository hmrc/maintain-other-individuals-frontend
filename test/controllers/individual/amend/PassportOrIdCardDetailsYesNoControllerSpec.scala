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

package controllers.individual.amend

import base.SpecBase
import models.{Name, NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.individual.{IndexPage, NamePage, PassportOrIdCardDetailsYesNoPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class PassportOrIdCardDetailsYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val name: Name = Name("Joe", None, "Bloggs")

  private val index = 0

  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers
    .set(NamePage, name)
    .success
    .value
    .set(IndexPage, index)
    .success
    .value

  private val mode = NormalMode

  private lazy val checkDetailsRoute =
    controllers.individual.amend.routes.CheckDetailsController.renderFromUserAnswers(index).url

  private lazy val passportOrIdCardDetailsYesNoRoute: String =
    controllers.individual.amend.routes.PassportOrIdCardDetailsYesNoController.onPageLoad(mode).url

  "PassportOrIdCardDetailsYesNo Controller" must {

    "redirect to Check Details for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()
    }

    "redirect to Check Details for GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(PassportOrIdCardDetailsYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()
    }

    "redirect to Check Details when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      val request =
        FakeRequest(POST, passportOrIdCardDetailsYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual checkDetailsRoute

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, passportOrIdCardDetailsYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, passportOrIdCardDetailsYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
