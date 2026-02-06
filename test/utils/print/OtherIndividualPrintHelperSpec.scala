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

package utils.print

import base.SpecBase
import controllers.individual.add.{routes => addRts}
import controllers.individual.{routes => rts}
import models._
import pages.individual._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class OtherIndividualPrintHelperSpec extends SpecBase {

  private val name: Name                 = Name("First", Some("Middle"), "Last")
  private val ukAddress: UkAddress       = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  private val nonUkAddress: NonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  private val helper = injector.instanceOf[OtherIndividualPrintHelper]

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name)
    .success
    .value
    .set(DateOfBirthYesNoPage, true)
    .success
    .value
    .set(DateOfBirthPage, LocalDate.of(2010, 10, 10))
    .success
    .value
    .set(CountryOfNationalityYesNoPage, true)
    .success
    .value
    .set(CountryOfNationalityUkYesNoPage, false)
    .success
    .value
    .set(CountryOfNationalityPage, "FR")
    .success
    .value
    .set(NationalInsuranceNumberYesNoPage, true)
    .success
    .value
    .set(NationalInsuranceNumberPage, "AA000000A")
    .success
    .value
    .set(CountryOfResidenceYesNoPage, true)
    .success
    .value
    .set(CountryOfResidenceUkYesNoPage, false)
    .success
    .value
    .set(CountryOfResidencePage, "FR")
    .success
    .value
    .set(AddressYesNoPage, true)
    .success
    .value
    .set(LiveInTheUkYesNoPage, true)
    .success
    .value
    .set(UkAddressPage, ukAddress)
    .success
    .value
    .set(NonUkAddressPage, nonUkAddress)
    .success
    .value
    .set(MentalCapacityYesNoPage, YesNoDontKnow.Yes)
    .success
    .value
    .set(WhenIndividualAddedPage, LocalDate.of(2020, 1, 1))
    .success
    .value

  "OtherIndividualPrintHelper" must {

    "generate other individual section for all possible data" when {

      "adding" in {

        val mode: Mode = NormalMode

        val userAnswers = baseAnswers
          .set(PassportDetailsYesNoPage, true)
          .success
          .value
          .set(PassportDetailsPage, Passport("GB", "123ABC456", LocalDate.of(2030, 10, 10)))
          .success
          .value
          .set(IdCardDetailsYesNoPage, true)
          .success
          .value
          .set(IdCardDetailsPage, IdCard("GB", "1", LocalDate.of(2030, 10, 10)))
          .success
          .value

        val result = helper(userAnswers, adding = true, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("otherIndividual.name.checkYourAnswersLabel"),
              answer = Html("First Middle Last"),
              changeUrl = Some(rts.NameController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.DateOfBirthYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.dateOfBirth.checkYourAnswersLabel", name.displayName),
              answer = Html("10 October 2010"),
              changeUrl = Some(rts.DateOfBirthController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfNationalityYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.CountryOfNationalityYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfNationalityUkYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("No"),
              changeUrl = Some(rts.CountryOfNationalityUkYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfNationality.checkYourAnswersLabel", name.displayName),
              answer = Html("France"),
              changeUrl = Some(rts.CountryOfNationalityController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName),
              answer = Html("AA 00 00 00 A"),
              changeUrl = Some(rts.NationalInsuranceNumberController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfResidenceYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.CountryOfResidenceYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfResidenceUkYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("No"),
              changeUrl = Some(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfResidence.checkYourAnswersLabel", name.displayName),
              answer = Html("France"),
              changeUrl = Some(rts.CountryOfResidenceController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.addressYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.AddressYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.LiveInTheUkYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.ukAddress.checkYourAnswersLabel", name.displayName),
              answer = Html("value 1<br />value 2<br />AB1 1AB"),
              changeUrl = Some(rts.UkAddressController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.nonUkAddress.checkYourAnswersLabel", name.displayName),
              answer = Html("value 1<br />value 2<br />Germany"),
              changeUrl = Some(rts.NonUkAddressController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.passportDetailsYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(addRts.PassportDetailsYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.passportDetails.checkYourAnswersLabel", name.displayName),
              answer = Html("United Kingdom<br />123ABC456<br />10 October 2030"),
              changeUrl = Some(addRts.PassportDetailsController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.idCardDetailsYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(addRts.IdCardDetailsYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.idCardDetails.checkYourAnswersLabel", name.displayName),
              answer = Html("United Kingdom<br />1<br />10 October 2030"),
              changeUrl = Some(addRts.IdCardDetailsController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.mentalCapacityYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.MentalCapacityYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.whenIndividualAdded.checkYourAnswersLabel", name.displayName),
              answer = Html("1 January 2020"),
              changeUrl = Some(addRts.WhenIndividualAddedController.onPageLoad(mode).url)
            )
          )
        )
      }

      "amending" in {

        val mode: Mode = CheckMode

        val userAnswers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, true)
          .success
          .value
          .set(PassportOrIdCardDetailsPage, CombinedPassportOrIdCard("GB", "123ABC456", LocalDate.of(2030, 10, 10)))
          .success
          .value

        val result = helper(userAnswers, adding = false, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("otherIndividual.name.checkYourAnswersLabel"),
              answer = Html("First Middle Last"),
              changeUrl = Some(rts.NameController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.DateOfBirthYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.dateOfBirth.checkYourAnswersLabel", name.displayName),
              answer = Html("10 October 2010"),
              changeUrl = Some(rts.DateOfBirthController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfNationalityYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.CountryOfNationalityYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfNationalityUkYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("No"),
              changeUrl = Some(rts.CountryOfNationalityUkYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfNationality.checkYourAnswersLabel", name.displayName),
              answer = Html("France"),
              changeUrl = Some(rts.CountryOfNationalityController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName),
              answer = Html("AA 00 00 00 A"),
              changeUrl = Some(rts.NationalInsuranceNumberController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfResidenceYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.CountryOfResidenceYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfResidenceUkYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("No"),
              changeUrl = Some(rts.CountryOfResidenceUkYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.countryOfResidence.checkYourAnswersLabel", name.displayName),
              answer = Html("France"),
              changeUrl = Some(rts.CountryOfResidenceController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.addressYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.AddressYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.LiveInTheUkYesNoController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.ukAddress.checkYourAnswersLabel", name.displayName),
              answer = Html("value 1<br />value 2<br />AB1 1AB"),
              changeUrl = Some(rts.UkAddressController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.nonUkAddress.checkYourAnswersLabel", name.displayName),
              answer = Html("value 1<br />value 2<br />Germany"),
              changeUrl = Some(rts.NonUkAddressController.onPageLoad(mode).url)
            ),
            AnswerRow(
              label = messages("otherIndividual.passportOrIdCardDetailsYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("otherIndividual.passportOrIdCardDetails.checkYourAnswersLabel", name.displayName),
              answer = Html("United Kingdom<br />Number ending C456<br />10 October 2030"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("otherIndividual.mentalCapacityYesNo.checkYourAnswersLabel", name.displayName),
              answer = Html("Yes"),
              changeUrl = Some(rts.MentalCapacityYesNoController.onPageLoad(mode).url)
            )
          )
        )
      }
    }
  }

}
