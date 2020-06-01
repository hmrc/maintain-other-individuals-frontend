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

package pages.behaviours

import java.time.LocalDate

import models.{IdCard, NonUkAddress, Passport, UkAddress}
import pages.individual._


class AddressYesNoPageSpec extends PageBehaviours {

  "AddressYesNoPage" must {

    beRetrievable[Boolean](AddressYesNoPage)

    beSettable[Boolean](AddressYesNoPage)

    beRemovable[Boolean](AddressYesNoPage)

    "implement cleanup logic when NO selected" in {
      val userAnswers = emptyUserAnswers
        .set(LiveInTheUkYesNoPage, false)
        .flatMap(_.set(UkAddressPage, UkAddress("line1", "line2", None, None, "postcode"))
        .flatMap(_.set(NonUkAddressPage, NonUkAddress("line1", "line2", None, "Germany")))
        .flatMap(_.set(PassportDetailsYesNoPage, true))
        .flatMap(_.set(PassportDetailsPage, Passport("GB", "12345",  LocalDate.now)))
        .flatMap(_.set(IdCardDetailsYesNoPage, true)))
        .flatMap(_.set(IdCardDetailsPage, IdCard("Germany", "12345", LocalDate.now)))
        .flatMap(_.set(AddressYesNoPage, false))

      userAnswers.get.get(LiveInTheUkYesNoPage) mustNot be(defined)
      userAnswers.get.get(UkAddressPage) mustNot be(defined)
      userAnswers.get.get(NonUkAddressPage) mustNot be(defined)
      userAnswers.get.get(PassportDetailsYesNoPage) mustNot be(defined)
      userAnswers.get.get(PassportDetailsPage) mustNot be(defined)
      userAnswers.get.get(IdCardDetailsYesNoPage) mustNot be(defined)
      userAnswers.get.get(IdCardDetailsPage) mustNot be(defined)

    }
  }
}
