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

import com.google.inject.Inject
import controllers.individual.add.{routes => addRts}
import controllers.individual.amend.{routes => amendRts}
import controllers.individual.{routes => rts}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.individual._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

class OtherIndividualPrintHelper @Inject() (answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, adding: Boolean, otherIndividualName: String)(implicit
    messages: Messages
  ): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, otherIndividualName)

    val changeLinkOrNone: (Boolean, String) => Option[String] =
      (adding: Boolean, route: String) => if (adding) Some(route) else None

    def answerRows(mode: Mode): Seq[AnswerRow] = Seq(
      bound.nameQuestion(NamePage, "otherIndividual.name", rts.NameController.onPageLoad(mode).url),
      bound.yesNoQuestion(
        DateOfBirthYesNoPage,
        "otherIndividual.dateOfBirthYesNo",
        rts.DateOfBirthYesNoController.onPageLoad(mode).url
      ),
      bound.dateQuestion(
        DateOfBirthPage,
        "otherIndividual.dateOfBirth",
        rts.DateOfBirthController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        CountryOfNationalityYesNoPage,
        "otherIndividual.countryOfNationalityYesNo",
        rts.CountryOfNationalityYesNoController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        CountryOfNationalityUkYesNoPage,
        "otherIndividual.countryOfNationalityUkYesNo",
        rts.CountryOfNationalityUkYesNoController.onPageLoad(mode).url
      ),
      bound.countryQuestion(
        CountryOfNationalityUkYesNoPage,
        CountryOfNationalityPage,
        "otherIndividual.countryOfNationality",
        rts.CountryOfNationalityController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        NationalInsuranceNumberYesNoPage,
        "otherIndividual.nationalInsuranceNumberYesNo",
        rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url
      ),
      bound.ninoQuestion(
        NationalInsuranceNumberPage,
        "otherIndividual.nationalInsuranceNumber",
        rts.NationalInsuranceNumberController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        CountryOfResidenceYesNoPage,
        "otherIndividual.countryOfResidenceYesNo",
        rts.CountryOfResidenceYesNoController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        CountryOfResidenceUkYesNoPage,
        "otherIndividual.countryOfResidenceUkYesNo",
        rts.CountryOfResidenceUkYesNoController.onPageLoad(mode).url
      ),
      bound.countryQuestion(
        CountryOfResidenceUkYesNoPage,
        CountryOfResidencePage,
        "otherIndividual.countryOfResidence",
        rts.CountryOfResidenceController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        AddressYesNoPage,
        "otherIndividual.addressYesNo",
        rts.AddressYesNoController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        LiveInTheUkYesNoPage,
        "otherIndividual.liveInTheUkYesNo",
        rts.LiveInTheUkYesNoController.onPageLoad(mode).url
      ),
      bound.addressQuestion(UkAddressPage, "otherIndividual.ukAddress", rts.UkAddressController.onPageLoad(mode).url),
      bound.addressQuestion(
        NonUkAddressPage,
        "otherIndividual.nonUkAddress",
        rts.NonUkAddressController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        PassportDetailsYesNoPage,
        "otherIndividual.passportDetailsYesNo",
        addRts.PassportDetailsYesNoController.onPageLoad(mode).url
      ),
      bound.passportDetailsQuestion(
        PassportDetailsPage,
        "otherIndividual.passportDetails",
        addRts.PassportDetailsController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        IdCardDetailsYesNoPage,
        "otherIndividual.idCardDetailsYesNo",
        addRts.IdCardDetailsYesNoController.onPageLoad(mode).url
      ),
      bound.idCardDetailsQuestion(
        IdCardDetailsPage,
        "otherIndividual.idCardDetails",
        addRts.IdCardDetailsController.onPageLoad(mode).url
      ),
      bound.yesNoQuestion(
        PassportOrIdCardDetailsYesNoPage,
        "otherIndividual.passportOrIdCardDetailsYesNo",
        changeLinkOrNone(adding, amendRts.PassportOrIdCardDetailsYesNoController.onPageLoad(CheckMode).url)
      ),
      bound.passportOrIdCardDetailsQuestion(
        PassportOrIdCardDetailsPage,
        "otherIndividual.passportOrIdCardDetails",
        changeLinkOrNone(adding, amendRts.PassportOrIdCardDetailsController.onPageLoad(CheckMode).url)
      ),
      bound.enumQuestion(
        MentalCapacityYesNoPage,
        "otherIndividual.mentalCapacityYesNo",
        rts.MentalCapacityYesNoController.onPageLoad(mode).url,
        "site"
      ),
      if (adding)
        bound.dateQuestion(
          WhenIndividualAddedPage,
          "otherIndividual.whenIndividualAdded",
          addRts.WhenIndividualAddedController.onPageLoad(mode).url
        )
      else None
    ).flatten

    val mode = if (adding) NormalMode else CheckMode
    AnswerSection(headingKey = None, rows = answerRows(mode))

  }

}
