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

package controllers.individual

import controllers.actions.{NameRequiredAction, StandardActionSets}
import forms.YesNoFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.individual.NationalInsuranceNumberYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.NationalInsuranceNumberYesNoView

import scala.concurrent.{ExecutionContext, Future}

class NationalInsuranceNumberYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: PlaybackRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  nameAction: NameRequiredAction,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NationalInsuranceNumberYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form: Form[Boolean] = formProvider.withPrefix("otherIndividual.nationalInsuranceNumberYesNo")

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      val preparedForm = request.userAnswers.get(NationalInsuranceNumberYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.otherIndividual, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.verifiedForUtr.andThen(nameAction).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, request.otherIndividual, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NationalInsuranceNumberYesNoPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(NationalInsuranceNumberYesNoPage, mode, updatedAnswers))
        )
    }

}
