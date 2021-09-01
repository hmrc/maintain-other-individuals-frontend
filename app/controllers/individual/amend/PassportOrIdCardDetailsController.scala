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

package controllers.individual.amend

import controllers.actions._
import forms.CombinedPassportOrIdCardDetailsFormProvider
import models.DetailsType.{Combined, CombinedProvisional}
import models.{CombinedPassportOrIdCard, Mode}
import navigation.Navigator
import pages.individual.PassportOrIdCardDetailsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.individual.amend.PassportOrIdCardDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PassportOrIdCardDetailsController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   playbackRepository: PlaybackRepository,
                                                   navigator: Navigator,
                                                   standardActionSets: StandardActionSets,
                                                   nameAction: NameRequiredAction,
                                                   formProvider: CombinedPassportOrIdCardDetailsFormProvider,
                                                   countryOptions: CountryOptions,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: PassportOrIdCardDetailsView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[CombinedPassportOrIdCard] = formProvider.withPrefix("otherIndividual.passportOrIdCardDetails")

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PassportOrIdCardDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.otherIndividual, countryOptions.options))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, request.otherIndividual, countryOptions.options))),

        newAnswer =>
          for {
            maybeOldAnswer <- Future.successful(request.userAnswers.get(PassportOrIdCardDetailsPage))
            detailsType = maybeOldAnswer match {
                case Some(oldAnswer) if oldAnswer.number == newAnswer.number && !oldAnswer.detailsType.isProvisional => Combined
                case _ => CombinedProvisional
              }

            updatedAnswers <- Future.fromTry(request.userAnswers.set(PassportOrIdCardDetailsPage, newAnswer.copy(detailsType = detailsType)))
            _              <- playbackRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PassportOrIdCardDetailsPage, mode, updatedAnswers))
      )
  }
}
