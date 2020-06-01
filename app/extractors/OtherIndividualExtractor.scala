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

package extractors

import com.google.inject.Inject
import models.{Address, IdCard, NationalInsuranceNumber, NonUkAddress, OtherIndividual, Passport, UkAddress, UserAnswers}
import pages._
import pages.individual.{AddressYesNoPage, DateOfBirthPage, DateOfBirthYesNoPage, IdCardDetailsPage, IdCardDetailsYesNoPage, IndexPage, LiveInTheUkYesNoPage, NamePage, NationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage, NonUkAddressPage, PassportDetailsPage, PassportDetailsYesNoPage, UkAddressPage, WhenIndividualAddedPage}

import scala.util.Try

class OtherIndividualExtractor @Inject()() {

  def apply(answers: UserAnswers, individual : OtherIndividual, index: Int): Try[UserAnswers] =
    answers.deleteAtPath(pages.individual.basePath)
      .flatMap(_.set(NamePage, individual.name))
      .flatMap(answers => extractDateOfBirth(individual, answers))
      .flatMap(answers => extractAddress(individual.address, answers))
      .flatMap(answers => extractIdentification(individual, answers))
      .flatMap(_.set(WhenIndividualAddedPage, individual.entityStart))
      .flatMap(_.set(IndexPage, index))

  private def extractAddress(address: Option[Address], answers: UserAnswers) : Try[UserAnswers] = {
    address match {
      case Some(uk: UkAddress) =>
        answers.set(AddressYesNoPage, true)
          .flatMap(_.set(LiveInTheUkYesNoPage, true))
          .flatMap(_.set(UkAddressPage, uk))
      case Some(nonUk: NonUkAddress) =>
        answers.set(AddressYesNoPage, true)
          .flatMap(_.set(LiveInTheUkYesNoPage, false))
          .flatMap(_.set(NonUkAddressPage, nonUk))
      case _ =>
        answers.set(AddressYesNoPage, false)
    }
  }

  private def extractDateOfBirth(individual: OtherIndividual, answers: UserAnswers) : Try[UserAnswers] =
  {
    individual.dateOfBirth match {
      case Some(dob) =>
        answers.set(DateOfBirthYesNoPage, true)
          .flatMap(_.set(DateOfBirthPage, dob))
      case None =>
        // Assumption that user answered no as dob is not provided
        answers.set(DateOfBirthYesNoPage, false)
    }
  }

  private def extractIdentification(individual: OtherIndividual,
                                    answers: UserAnswers) : Try[UserAnswers] =
  {
    individual.identification match {
      case Some(NationalInsuranceNumber(nino)) =>
        answers.set(NationalInsuranceNumberYesNoPage, true)
          .flatMap(_.set(NationalInsuranceNumberPage, nino))
      case Some(p : Passport) =>
        answers.set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(PassportDetailsYesNoPage, true))
          .flatMap(_.set(PassportDetailsPage, p))
      case Some(id: IdCard) =>
        answers.set(NationalInsuranceNumberYesNoPage, false)
          .flatMap(_.set(IdCardDetailsYesNoPage, true))
          .flatMap(_.set(IdCardDetailsPage, id))
      case _ =>
        answers.set(NationalInsuranceNumberYesNoPage, false)
    }
  }
}
