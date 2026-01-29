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

package controllers.individual.add

import controllers.actions.{NameRequiredAction, StandardActionSets}
import forms.DateAddedToTrustFormProvider
import models.Mode
import models.requests.OtherIndividualNameRequest
import navigation.Navigator
import pages.individual.{DateOfBirthPage, WhenIndividualAddedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.add.WhenIndividualAddedView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhenIndividualAddedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: PlaybackRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  nameAction: NameRequiredAction,
  formProvider: DateAddedToTrustFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhenIndividualAddedView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>
      val form = formProvider.withConfig("otherIndividual.whenIndividualAdded", minDate)

      val preparedForm = request.userAnswers.get(WhenIndividualAddedPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.otherIndividual))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>
      val form = formProvider.withConfig("otherIndividual.whenIndividualAdded", minDate)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.otherIndividual))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhenIndividualAddedPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhenIndividualAddedPage, mode, updatedAnswers))
        )
  }

  private def minDate(implicit request: OtherIndividualNameRequest[AnyContent]): LocalDate = {
    val startDate = request.userAnswers.whenTrustSetup
    request.userAnswers.get(DateOfBirthPage) match {
      case Some(dob) if dob.isAfter(startDate) => dob
      case _                                   => startDate
    }
  }

}
