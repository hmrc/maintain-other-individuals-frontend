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

package pages.individual

import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class NationalInsuranceNumberYesNoPageSpec extends PageBehaviours with ScalaCheckPropertyChecks with Generators {

  private val nino: String = "nino"
  private val ukAddress: UkAddress = UkAddress("line1", "line2", None, None, "postcode")
  private val nonUkAddress: NonUkAddress = NonUkAddress("line1", "line2", None, "country")
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val passport: Passport = Passport("country", "number", date)
  private val idCard: IdCard = IdCard("country", "number", date)
  private val passportOrIdCard: CombinedPassportOrIdCard = CombinedPassportOrIdCard("country", "number", date)

  "NationalInsuranceNumberYesNo Page" must {

    beRetrievable[Boolean](NationalInsuranceNumberYesNoPage)

    beSettable[Boolean](NationalInsuranceNumberYesNoPage)

    beRemovable[Boolean](NationalInsuranceNumberYesNoPage)

    "implement cleanup logic when YES selected" in {

      forAll(arbitrary[UserAnswers]) {
        arbitraryAnswers =>
          val userAnswers: UserAnswers = arbitraryAnswers
            .set(AddressYesNoPage, true).success.value
            .set(LiveInTheUkYesNoPage, true).success.value
            .set(UkAddressPage, ukAddress).success.value
            .set(NonUkAddressPage, nonUkAddress).success.value
            .set(PassportDetailsYesNoPage, true).success.value
            .set(PassportDetailsPage, passport).success.value
            .set(IdCardDetailsYesNoPage, true).success.value
            .set(IdCardDetailsPage, idCard).success.value
            .set(PassportOrIdCardDetailsYesNoPage, true).success.value
            .set(PassportOrIdCardDetailsPage, passportOrIdCard).success.value

          val result: UserAnswers = userAnswers.set(NationalInsuranceNumberYesNoPage, true).success.value

          result.get(AddressYesNoPage) mustNot be(defined)
          result.get(LiveInTheUkYesNoPage) mustNot be(defined)
          result.get(UkAddressPage) mustNot be(defined)
          result.get(NonUkAddressPage) mustNot be(defined)
          result.get(PassportDetailsYesNoPage) mustNot be(defined)
          result.get(PassportDetailsPage) mustNot be(defined)
          result.get(IdCardDetailsYesNoPage) mustNot be(defined)
          result.get(IdCardDetailsPage) mustNot be(defined)
          result.get(PassportOrIdCardDetailsYesNoPage) mustNot be(defined)
          result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
      }
    }

    "implement cleanup logic when NO selected" in {

      forAll(arbitrary[UserAnswers]) {
        arbitraryAnswers =>
          val userAnswers: UserAnswers = arbitraryAnswers
            .set(NationalInsuranceNumberPage, nino).success.value

          val result: UserAnswers = userAnswers.set(NationalInsuranceNumberYesNoPage, false).success.value

          result.get(NationalInsuranceNumberPage) mustNot be(defined)
      }
    }
  }
}
