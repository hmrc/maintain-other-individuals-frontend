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

package views.individual.add

import forms.DateAddedToTrustFormProvider
import models.{Name, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.individual.add.WhenIndividualAddedView

import java.time.LocalDate

class WhenIndividualAddedViewSpec extends QuestionViewBehaviours[LocalDate] {

  val messageKeyPrefix = "otherIndividual.whenIndividualAdded"
  val date: LocalDate = LocalDate.parse("2019-02-03")
  val form: Form[LocalDate] = new DateAddedToTrustFormProvider().withConfig(messageKeyPrefix, date)
  val view: WhenIndividualAddedView = viewFor[WhenIndividualAddedView](Some(emptyUserAnswers))
  val name: Name = Name("First", Some("Middle"), "Last")

  "WhenIndividualAdded view" must {

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, name.displayName)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name.displayName)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithHint(form, applyView, messageKeyPrefix + ".hint")

    behave like pageWithDateFields(
      form,
      applyView,
      messageKeyPrefix,
      "value"
    )

    behave like pageWithASubmitButton(applyView(form))
  }

}
