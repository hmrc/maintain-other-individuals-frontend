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

package views.components

import base.SpecBase
import play.api.data.Form
import play.api.data.Forms.text

class InputEmailSpec extends SpecBase {

  val form: Form[String] = Form("value" -> text())
  "Input email component  " should {

    "render as a Gov.UK input with email as type" in {
      val application = applicationBuilder().build()

      val view = application.injector.instanceOf[views.html.components.InputEmail]

      val result = view.apply(
        field = form("value"),
        label = "test label")(messages)

      result.body must include ("govuk-input")
      result.body must include ("type=\"email\"")
      result.body must include ("autocomplete=\"email\"")
    }

  }
}
