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

package controllers.individual.remove

import controllers.actions.StandardActionSets
import forms.DateRemovedFromTrustFormProvider
import javax.inject.Inject
import models.RemoveOtherIndividual
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.remove.WhenRemovedView

import scala.concurrent.{ExecutionContext, Future}

class WhenRemovedController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       formProvider: DateRemovedFromTrustFormProvider,
                                       trust: TrustService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: WhenRemovedView,
                                       trustService: TrustService
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getOtherIndividual(request.userAnswers.identifier, index).map {
        otherIndividual =>
          val form = formProvider.withPrefixAndEntityStartDate("otherIndividual.whenRemoved", otherIndividual.entityStart)
          Ok(view(form, index, otherIndividual.name.displayName))
      }
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trust.getOtherIndividual(request.userAnswers.identifier, index).flatMap {
        otherIndividual =>
          val form = formProvider.withPrefixAndEntityStartDate("otherIndividual.whenRemoved", otherIndividual.entityStart)
          form.bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, index, otherIndividual.name.displayName)))
            },
            value =>
              trustService.removeOtherIndividual(request.userAnswers.identifier, RemoveOtherIndividual(index, value)).map(_ =>
                Redirect(controllers.routes.AddAnOtherIndividualController.onPageLoad())
              )
          )
      }
  }
}
