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

package extractors

import com.google.inject.Inject
import models.Constant.GB
import models.{Address, CombinedPassportOrIdCard, IdCard, IndividualIdentification, NationalInsuranceNumber, NonUkAddress, OtherIndividual, Passport, UkAddress, UserAnswers}
import pages.individual.{AddressYesNoPage, CountryOfNationalityPage, CountryOfNationalityUkYesNoPage, CountryOfNationalityYesNoPage, CountryOfResidencePage, CountryOfResidenceUkYesNoPage, CountryOfResidenceYesNoPage, DateOfBirthPage, DateOfBirthYesNoPage, IdCardDetailsPage, IdCardDetailsYesNoPage, IndexPage, LiveInTheUkYesNoPage, MentalCapacityYesNoPage, NamePage, NationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage, NonUkAddressPage, PassportDetailsPage, PassportDetailsYesNoPage, PassportOrIdCardDetailsPage, PassportOrIdCardDetailsYesNoPage, ProvisionalIdDetailsPage, UkAddressPage, WhenIndividualAddedPage}

import scala.util.{Success, Try}

class OtherIndividualExtractor @Inject()() {

  def apply(answers: UserAnswers, individual: OtherIndividual, index: Int): Try[UserAnswers] =
    answers.deleteAtPath(pages.individual.basePath)
      .flatMap(_.set(NamePage, individual.name))
      .flatMap(answers => extractDateOfBirth(individual, answers))
      .flatMap(answers => extractCountryOfNationality(individual.countryOfNationality, answers))
      .flatMap(answers => extractCountryOfResidence(individual.countryOfResidence, answers))
      .flatMap(answers => extractAddress(individual.address, answers))
      .flatMap(answers => extractIdentification(individual, answers))
      .flatMap(_.set(MentalCapacityYesNoPage, individual.mentalCapacityYesNo))
      .flatMap(_.set(WhenIndividualAddedPage, individual.entityStart))
      .flatMap(_.set(IndexPage, index))
      .flatMap(answers => extractIfIdDetailsAreProvisional(individual.identification, answers))

  private def extractCountryOfNationality(countryOfNationality: Option[String], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.is5mldEnabled && answers.isUnderlyingData5mld) {
      countryOfNationality match {
        case Some(GB) => answers
          .set(CountryOfNationalityYesNoPage, true)
          .flatMap(_.set(CountryOfNationalityUkYesNoPage, true))
          .flatMap(_.set(CountryOfNationalityPage, GB))
        case Some(country) => answers
          .set(CountryOfNationalityYesNoPage, true)
          .flatMap(_.set(CountryOfNationalityUkYesNoPage, false))
          .flatMap(_.set(CountryOfNationalityPage, country))
        case None => answers
          .set(CountryOfNationalityYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  private def extractCountryOfResidence(countryOfResidence: Option[String], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.is5mldEnabled && answers.isUnderlyingData5mld) {
      countryOfResidence match {
        case Some(GB) => answers
          .set(CountryOfResidenceYesNoPage, true)
          .flatMap(_.set(CountryOfResidenceUkYesNoPage, true))
          .flatMap(_.set(CountryOfResidencePage, GB))
        case Some(country) => answers
          .set(CountryOfResidenceYesNoPage, true)
          .flatMap(_.set(CountryOfResidenceUkYesNoPage, false))
          .flatMap(_.set(CountryOfResidencePage, country))
        case None => answers
          .set(CountryOfResidenceYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  private def extractAddress(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable) {
      address match {
        case Some(uk: UkAddress) => answers
          .set(AddressYesNoPage, true)
          .flatMap(_.set(LiveInTheUkYesNoPage, true))
          .flatMap(_.set(UkAddressPage, uk))
        case Some(nonUk: NonUkAddress) => answers
          .set(AddressYesNoPage, true)
          .flatMap(_.set(LiveInTheUkYesNoPage, false))
          .flatMap(_.set(NonUkAddressPage, nonUk))
        case _ =>
          answers.set(AddressYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  private def extractDateOfBirth(individual: OtherIndividual, answers: UserAnswers): Try[UserAnswers] = {
    individual.dateOfBirth match {
      case Some(dob) => answers
        .set(DateOfBirthYesNoPage, true)
        .flatMap(_.set(DateOfBirthPage, dob))
      case None => answers
        .set(DateOfBirthYesNoPage, false)
    }
  }

  private def extractIdentification(individual: OtherIndividual,
                                    answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable) {
      individual.identification match {
        case Some(NationalInsuranceNumber(nino)) => answers
          .set(NationalInsuranceNumberYesNoPage, true)
          .flatMap(_.set(NationalInsuranceNumberPage, nino))
        case Some(p: Passport) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportDetailsYesNoPage, true))
          .flatMap(_.set(PassportDetailsPage, p))
        case Some(id: IdCard) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(IdCardDetailsYesNoPage, true))
          .flatMap(_.set(IdCardDetailsPage, id))
        case Some(combined: CombinedPassportOrIdCard) => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
          .flatMap(_.set(PassportOrIdCardDetailsPage, combined))
        case _ => answers
          .set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(answers => extractPassportOrIdCardDetailsYesNo(individual.address, answers))
      }
    } else {
      Success(answers)
    }
  }

  private def extractPassportOrIdCardDetailsYesNo(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    if (address.isDefined) {
      answers.set(PassportDetailsYesNoPage, false)
        .flatMap(_.set(IdCardDetailsYesNoPage, false))
    } else {
      Success(answers)
    }
  }

  // `identification` is an instance of CombinedPassportOrIdCard => not added in session (i.e. not provisional)
  // `identification` is an instance of Passport or IdCard => added in session (i.e. provisional)
  // `identification` is empty or an instance of NationalInsuranceNumber => nothing to set
  private def extractIfIdDetailsAreProvisional(identification: Option[IndividualIdentification], answers: UserAnswers): Try[UserAnswers] = {
    val provisional: Option[Boolean] = identification match {
      case Some(_: CombinedPassportOrIdCard) => Some(false)
      case Some(_: Passport) | Some(_: IdCard) => Some(true)
      case _ => None
    }
    answers.set(ProvisionalIdDetailsPage, provisional)
  }

}
