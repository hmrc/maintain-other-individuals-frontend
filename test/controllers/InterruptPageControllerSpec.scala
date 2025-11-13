/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers._

class InterruptPageControllerSpec extends SpecBase {

  "InterruptPage Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder().build()

      val request = FakeRequest(GET, routes.InterruptPageController.taxablePageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "redirect to name page for a POST" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, routes.InterruptPageController.onSubmit.url)
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual "/maintain-a-trust/other-individuals/name"

      application.stop()

    }

    "return OK and the non taxable information view for a GET" in {

      val application = applicationBuilder().build()

      val request = FakeRequest(GET, routes.InterruptPageController.nonTaxablePageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

  }
}
