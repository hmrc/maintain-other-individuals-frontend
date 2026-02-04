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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem

class InputCheckboxesSpec extends SpecBase {

  val form: Form[String] = Form("value" -> text())
  val items              = Seq(CheckboxItem(content = Text("option 1"), value = "option1"))

  "Input check boxes component" should {

    "render checkboxes with legend and items" in {
      val application = applicationBuilder().build()

      val view = application.injector.instanceOf[views.html.components.InputCheckboxes]

      val result = view.apply(
        field = form("value"),
        legend = "choose your options",
        legendClass = Some("govuk-fieldset__legend--l"),
        hint = Some("select all the options"),
        inputs = items
      )(messages)

      result.body must include("option 1")
      result.body must include("govuk-hint")
      result.body must include("govuk-checkboxes")
    }

  }

}
