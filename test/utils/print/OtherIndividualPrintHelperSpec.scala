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

package utils.print

import java.time.LocalDate

import base.SpecBase
import controllers.individual.add.{routes => addRts}
import controllers.individual.{routes => rts}
import models.{IdCard, Name, NonUkAddress, NormalMode, Passport, UkAddress}
import pages.individual._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class OtherIndividualPrintHelperSpec extends SpecBase {

  val name: Name = Name("First", Some("Middle"), "Last")
  val ukAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  val nonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  "AddOtherIndividualPrintHelper" must {

    "generate other individual section for all possible data" in {

      val helper = injector.instanceOf[OtherIndividualPrintHelper]

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(DateOfBirthYesNoPage, true).success.value
        .set(DateOfBirthPage, LocalDate.of(2010, 10, 10)).success.value
        .set(NationalInsuranceNumberYesNoPage, true).success.value
        .set(NationalInsuranceNumberPage, "AA000000A").success.value
        .set(AddressYesNoPage, true).success.value
        .set(LiveInTheUkYesNoPage, true).success.value
        .set(UkAddressPage, ukAddress).success.value
        .set(NonUkAddressPage, nonUkAddress).success.value
        .set(PassportDetailsYesNoPage, true).success.value
        .set(PassportDetailsPage, Passport("GB", "1", LocalDate.of(2030, 10, 10))).success.value
        .set(IdCardDetailsYesNoPage, true).success.value
        .set(IdCardDetailsPage, IdCard("GB", "1", LocalDate.of(2030, 10, 10))).success.value
        .set(WhenIndividualAddedPage, LocalDate.of(2020, 1, 1)).success.value

      val result = helper(userAnswers, provisional = true, name.displayName)
      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = Html(messages("otherIndividual.name.checkYourAnswersLabel")), answer = Html("First Middle Last"), changeUrl = rts.NameController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.DateOfBirthYesNoController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.dateOfBirth.checkYourAnswersLabel", name.displayName)), answer = Html("10 October 2010"), changeUrl = rts.DateOfBirthController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = rts.NationalInsuranceNumberController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.addressYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.AddressYesNoController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.LiveInTheUkYesNoController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.ukAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = rts.UkAddressController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.nonUkAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = rts.NonUkAddressController.onPageLoad(NormalMode).url),
          AnswerRow(label = Html(messages("otherIndividual.passportDetailsYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = addRts.PassportDetailsYesNoController.onPageLoad().url),
          AnswerRow(label = Html(messages("otherIndividual.passportDetails.checkYourAnswersLabel", name.displayName)), answer = Html("United Kingdom<br />1<br />10 October 2030"), changeUrl = addRts.PassportDetailsController.onPageLoad().url),
          AnswerRow(label = Html(messages("otherIndividual.idCardDetailsYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = addRts.IdCardDetailsYesNoController.onPageLoad().url),
          AnswerRow(label = Html(messages("otherIndividual.idCardDetails.checkYourAnswersLabel", name.displayName)), answer = Html("United Kingdom<br />1<br />10 October 2030"), changeUrl = addRts.IdCardDetailsController.onPageLoad().url),
          AnswerRow(label = Html(messages("otherIndividual.whenIndividualAdded.checkYourAnswersLabel", name.displayName)), answer = Html("1 January 2020"), changeUrl = addRts.WhenIndividualAddedController.onPageLoad().url)
        )
      )
    }
  }
}