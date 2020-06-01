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

package navigation

import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.individual.{AddressYesNoPage, DateOfBirthPage, DateOfBirthYesNoPage, IdCardDetailsPage, IdCardDetailsYesNoPage, IndexPage, LiveInTheUkYesNoPage, NamePage, NationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage, NonUkAddressPage, PassportDetailsPage, PassportDetailsYesNoPage, UkAddressPage, WhenIndividualAddedPage}
import pages.{Page, QuestionPage}
import play.api.mvc.Call

class OtherIndividualNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, Call] = {
    case NamePage => controllers.routes.DateOfBirthYesNoController.onPageLoad(mode)
    case DateOfBirthPage => controllers.routes.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    case UkAddressPage => controllers.routes.PassportDetailsYesNoController.onPageLoad(mode)
    case NonUkAddressPage => controllers.routes.PassportDetailsYesNoController.onPageLoad(mode)
    case WhenIndividualAddedPage => controllers.routes.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case DateOfBirthYesNoPage => ua =>
      yesNoNav(ua, DateOfBirthYesNoPage, controllers.routes.DateOfBirthController.onPageLoad(mode), controllers.routes.NationalInsuranceNumberYesNoController.onPageLoad(mode))
    case NationalInsuranceNumberYesNoPage => ua =>
      yesNoNav(ua, NationalInsuranceNumberYesNoPage, controllers.routes.NationalInsuranceNumberController.onPageLoad(mode), controllers.routes.AddressYesNoController.onPageLoad(mode))
    case LiveInTheUkYesNoPage => ua =>
      yesNoNav(ua, LiveInTheUkYesNoPage, controllers.routes.UkAddressController.onPageLoad(mode), controllers.routes.NonUkAddressController.onPageLoad(mode))
    case PassportDetailsYesNoPage => ua =>
      yesNoNav(ua, PassportDetailsYesNoPage, controllers.routes.PassportDetailsController.onPageLoad(mode), controllers.routes.IdCardDetailsYesNoController.onPageLoad(mode))
  }

  private def navigationWithCheck(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    mode match {
      case NormalMode => {
        case NationalInsuranceNumberPage | PassportDetailsPage | IdCardDetailsPage  => _ =>
          controllers.routes.WhenIndividualAddedController.onPageLoad()
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, controllers.routes.LiveInTheUkYesNoController.onPageLoad(mode), controllers.routes.WhenIndividualAddedController.onPageLoad())
        case IdCardDetailsYesNoPage => ua =>
          yesNoNav(ua, IdCardDetailsYesNoPage, controllers.routes.IdCardDetailsController.onPageLoad(mode), controllers.routes.WhenIndividualAddedController.onPageLoad())
      }
      case CheckMode => {
        case NationalInsuranceNumberPage | PassportDetailsPage | IdCardDetailsPage => ua =>
          checkDetailsRoute(ua)
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, controllers.routes.LiveInTheUkYesNoController.onPageLoad(mode), checkDetailsRoute(ua))
        case IdCardDetailsYesNoPage => ua =>
          yesNoNav(ua, IdCardDetailsYesNoPage, controllers.routes.IdCardDetailsController.onPageLoad(mode), checkDetailsRoute(ua))
      }
    }
  }

  def yesNoNav(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call = {
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.SessionExpiredController.onPageLoad())
  }

  def checkDetailsRoute(answers: UserAnswers): Call = {
    answers.get(IndexPage) match {
      case None => controllers.routes.SessionExpiredController.onPageLoad()
      case _ =>
        controllers.routes.FeatureNotAvailableController.onPageLoad()
    }
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) andThen (c => (_: UserAnswers) => c) orElse
      yesNoNavigation(mode)  orElse
      navigationWithCheck(mode)

}

