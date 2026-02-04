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

package utils.mappers

import models.Constant.GB

import java.time.LocalDate
import models._
import pages.individual._
import play.api.Logging
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsSuccess, Reads}

class OtherIndividualMapper extends Logging {

  def apply(answers: UserAnswers): Option[OtherIndividual] = {
    val readFromUserAnswers: Reads[OtherIndividual] =
      (
        NamePage.path.read[Name] and
          DateOfBirthPage.path.readNullable[LocalDate] and
          readCountryOfNationality and
          readCountryOfResidence and
          readIdentification and
          readAddress and
          readMentalCapacity and
          WhenIndividualAddedPage.path.read[LocalDate] and
          Reads(_ => JsSuccess(true))
      )(OtherIndividual.apply _)

    answers.data.validate[OtherIndividual](readFromUserAnswers) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors)     =>
        logger.error(
          s"[Mapper][UTR: ${answers.identifier}] Failed to rehydrate OtherIndividual from UserAnswers due to ${JsError.toJson(errors)}"
        )
        None
    }
  }

  private def readCountryOfNationality: Reads[Option[String]] =
    CountryOfNationalityYesNoPage.path.readNullable[Boolean].flatMap[Option[String]] {
      case Some(true) =>
        CountryOfNationalityUkYesNoPage.path.read[Boolean].flatMap {
          case true  => Reads(_ => JsSuccess(Some(GB)))
          case false => CountryOfNationalityPage.path.read[String].map(Some(_))
        }
      case _          => Reads(_ => JsSuccess(None))
    }

  private def readCountryOfResidence: Reads[Option[String]] =
    CountryOfResidenceYesNoPage.path.readNullable[Boolean].flatMap[Option[String]] {
      case Some(true) =>
        CountryOfResidenceUkYesNoPage.path.read[Boolean].flatMap {
          case true  => Reads(_ => JsSuccess(Some(GB)))
          case false => CountryOfResidencePage.path.read[String].map(Some(_))
        }
      case _          => Reads(_ => JsSuccess(None))
    }

  private def readIdentification: Reads[Option[IndividualIdentification]] =
    NationalInsuranceNumberYesNoPage.path.readNullable[Boolean].flatMap[Option[IndividualIdentification]] {
      case Some(true)  => NationalInsuranceNumberPage.path.read[String].map(nino => Some(NationalInsuranceNumber(nino)))
      case Some(false) => readPassportOrIdCard
      case _           => Reads(_ => JsSuccess(None))
    }

  private def readPassportOrIdCard: Reads[Option[IndividualIdentification]] = {
    val identification = for {
      hasNino             <- NationalInsuranceNumberYesNoPage.path.readWithDefault(false)
      hasAddress          <- AddressYesNoPage.path.readWithDefault(false)
      hasPassport         <- PassportDetailsYesNoPage.path.readWithDefault(false)
      hasIdCard           <- IdCardDetailsYesNoPage.path.readWithDefault(false)
      hasPassportOrIdCard <- PassportOrIdCardDetailsYesNoPage.path.readWithDefault(false)
    } yield (hasNino, hasAddress, hasPassport, hasIdCard, hasPassportOrIdCard)

    identification.flatMap[Option[IndividualIdentification]] {
      case (false, true, true, false, _)     => PassportDetailsPage.path.read[Passport].map(x => x.asCombined).map(Some(_))
      case (false, true, false, true, _)     => IdCardDetailsPage.path.read[IdCard].map(x => x.asCombined).map(Some(_))
      case (false, true, false, false, true) =>
        PassportOrIdCardDetailsPage.path.read[CombinedPassportOrIdCard].map(Some(_))
      case _                                 => Reads(_ => JsSuccess(None))
    }
  }

  private def readAddress: Reads[Option[Address]] =
    NationalInsuranceNumberYesNoPage.path.readNullable[Boolean].flatMap {
      case Some(true)  => Reads(_ => JsSuccess(None))
      case Some(false) =>
        AddressYesNoPage.path.read[Boolean].flatMap[Option[Address]] {
          case true  => readUkOrNonUkAddress
          case false => Reads(_ => JsSuccess(None))
        }
      case _           => Reads(_ => JsSuccess(None))
    }

  private def readUkOrNonUkAddress: Reads[Option[Address]] =
    LiveInTheUkYesNoPage.path.read[Boolean].flatMap[Option[Address]] {
      case true  => UkAddressPage.path.read[UkAddress].map(Some(_))
      case false => NonUkAddressPage.path.read[NonUkAddress].map(Some(_))
    }

  private def readMentalCapacity: Reads[Option[YesNoDontKnow]] =
    MentalCapacityYesNoPage.path
      .readNullable[YesNoDontKnow]
      .flatMap[Option[YesNoDontKnow]] {
        case Some(value) => Reads(_ => JsSuccess(Some(value)))
        case _           => Reads(_ => JsSuccess(None))
      }

}
