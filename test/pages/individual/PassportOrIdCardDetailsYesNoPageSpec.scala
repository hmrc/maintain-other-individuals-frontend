/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.LocalDate

import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.behaviours.PageBehaviours

class PassportOrIdCardDetailsYesNoPageSpec extends PageBehaviours with ScalaCheckPropertyChecks with Generators {

  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val passportOrIdCard: CombinedPassportOrIdCard = CombinedPassportOrIdCard("country", "number", date)

  "PassportOrIdCardDetailsYesNo Page" must {

    beRetrievable[Boolean](PassportOrIdCardDetailsYesNoPage)

    beSettable[Boolean](PassportOrIdCardDetailsYesNoPage)

    beRemovable[Boolean](PassportOrIdCardDetailsYesNoPage)

    "implement cleanup logic when NO selected" in {

      forAll(arbitrary[UserAnswers]) {
        arbitraryAnswers =>
          val userAnswers: UserAnswers = arbitraryAnswers
            .set(PassportOrIdCardDetailsPage, passportOrIdCard).success.value

          val result: UserAnswers = userAnswers.set(PassportOrIdCardDetailsYesNoPage, false).success.value

          result.get(PassportOrIdCardDetailsPage) mustNot be(defined)
      }
    }
  }
}
