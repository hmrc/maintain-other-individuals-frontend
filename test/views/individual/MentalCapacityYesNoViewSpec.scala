/*
 * Copyright 2021 HM Revenue & Customs
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

package views.individual

import controllers.individual.routes
import forms.{YesNoDontKnowFormProvider, YesNoFormProvider}
import models.{Name, NormalMode, YesNoDontKnow}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.RadioOption
import views.behaviours.{OptionsViewBehaviours, QuestionViewBehaviours, YesNoViewBehaviours}
import views.html.individual.MentalCapacityYesNoView

class MentalCapacityYesNoViewSpec extends OptionsViewBehaviours {

  val messageKeyPrefix = "otherIndividual.mentalCapacityYesNo"
  val name: Name = Name("First", None, "Last")

  val form: Form[YesNoDontKnow] = new YesNoDontKnowFormProvider().withPrefix(messageKeyPrefix)

  "MentalCapacityYesNoView" must {

    val view = viewFor[MentalCapacityYesNoView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, name.displayName)(fakeRequest, messages)

    val options = List(
      RadioOption(id = "value-yes", value = YesNoDontKnow.Yes.toString, messageKey = "site.yes"),
      RadioOption(id = "value-no", value = YesNoDontKnow.No.toString, messageKey = "site.no"),
      RadioOption(id = "value-dontKnow", value = YesNoDontKnow.DontKnow.toString, messageKey = "site.dontKnow")
    )

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name.displayName,"p1", "bulletpoint1", "bulletpoint2", "bulletpoint3", "bulletpoint4")

    behave like pageWithBackLink(applyView(form))

    behave like pageWithOptions[YesNoDontKnow](form, applyView, options)

    behave like pageWithASubmitButton(applyView(form))
  }
}
