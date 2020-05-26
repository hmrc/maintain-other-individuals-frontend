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

package controllers.individual.remove

import controllers.actions.StandardActionSets
import forms.YesNoFormProvider
import javax.inject.Inject
import models.RemoveOtherIndividual
import pages.individual.RemoveYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.individual.remove.RemoveOtherIndividualView

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ExecutionContext, Future}

class RemoveOtherIndividualController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     repository: PlaybackRepository,
                                                     standardActionSets: StandardActionSets,
                                                     trustService: TrustService,
                                                     formProvider: YesNoFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: RemoveOtherIndividualView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val messagesPrefix: String = "removeOtherIndividual"

  private val form = formProvider.withPrefix(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      val preparedForm = request.userAnswers.get(RemoveYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      trustService.getOtherIndividual(request.userAnswers.utr, index).map {
        otherIndividual =>
          Ok(view(preparedForm, index, otherIndividual.name.displayName))
      }

  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          trustService.getOtherIndividual(request.userAnswers.utr, index).map {
            otherIndividual =>
              BadRequest(view(formWithErrors, index, otherIndividual.name.displayName))
          }
        },
        value => {

          if (value) {

            trustService.getOtherIndividual(request.userAnswers.utr, index).flatMap {
              otherIndividual =>
                if (otherIndividual.provisional) {
                  trustService.removeOtherIndividual(request.userAnswers.utr, RemoveOtherIndividual(index)).map(_ =>
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
            }
          } else {
            Future.successful(Redirect(controllers.routes.AddAnOtherIndividualController.onPageLoad().url))
          }
        }
      )
  }
}