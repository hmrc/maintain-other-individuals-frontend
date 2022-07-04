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

package controllers.individual.remove

import controllers.actions.StandardActionSets
import forms.YesNoFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.RemoveOtherIndividual
import pages.individual.RemoveYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.remove.RemoveOtherIndividualView

import scala.concurrent.{ExecutionContext, Future}

class RemoveOtherIndividualController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     repository: PlaybackRepository,
                                                     standardActionSets: StandardActionSets,
                                                     trustService: TrustService,
                                                     formProvider: YesNoFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: RemoveOtherIndividualView,
                                                     errorHandler: ErrorHandler
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messagesPrefix: String = "removeOtherIndividualYesNo"

  private val form = formProvider.withPrefix(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      val preparedForm = request.userAnswers.get(RemoveYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      trustService.getOtherIndividual(request.userAnswers.identifier, index).map {
        otherIndividual =>
          Ok(view(preparedForm, index, otherIndividual.name.displayName))
      } recoverWith {
        case iobe: IndexOutOfBoundsException =>
          logger.warn(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error showing the user the other individual to remove for index $index from trusts service ${iobe.getMessage}: IndexOutOfBoundsException")

          Future.successful(Redirect(controllers.routes.AddAnOtherIndividualController.onPageLoad()))
        case _ =>
          logger.error(s"[Remove Individual][UTR: ${request.userAnswers.identifier}][Session ID: ${utils.Session.id(hc)}]" +
            s" no other individual found in trusts service to remove")
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }

  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          trustService.getOtherIndividual(request.userAnswers.identifier, index).map {
            otherIndividual =>
              BadRequest(view(formWithErrors, index, otherIndividual.name.displayName))
          }
        },
        value => {

          if (value) {

            trustService.getOtherIndividual(request.userAnswers.identifier, index).flatMap {
              otherIndividual =>
                if (otherIndividual.provisional) {
                  trustService.removeOtherIndividual(request.userAnswers.identifier, RemoveOtherIndividual(index)).map(_ =>
                    Redirect(controllers.routes.AddAnOtherIndividualController.onPageLoad())
                  )
                } else {
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveYesNoPage, value))
                    _ <- repository.set(updatedAnswers)
                  } yield {
                    Redirect(controllers.individual.remove.routes.WhenRemovedController.onPageLoad(index).url)
                  }
                }
            } recoverWith {
              case _ =>
                logger.error(s"[Remove Individual][UTR: ${request.userAnswers.identifier}][Session ID: ${utils.Session.id(hc)}]" +
                  s" no other individual found in trusts service to remove")
                Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
            }
          } else {
            Future.successful(Redirect(controllers.routes.AddAnOtherIndividualController.onPageLoad.url))
          }
        }
      )
  }
}
