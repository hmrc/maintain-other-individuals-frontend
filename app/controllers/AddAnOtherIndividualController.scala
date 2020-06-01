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

package controllers

import config.FrontendAppConfig
import connectors.TrustStoreConnector
import controllers.actions.StandardActionSets
import forms.{AddAnOtherIndividualFormProvider, YesNoFormProvider}
import javax.inject.Inject
import models.{AddAnOtherIndividual, NormalMode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.AddAnOtherIndividualViewHelper
import views.html.{AddAnOtherIndividualView, AddAnOtherIndividualYesNoView, MaxedOutOtherIndividualsView}

import scala.concurrent.{ExecutionContext, Future}

class AddAnOtherIndividualController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                standardActionSets: StandardActionSets,
                                                val controllerComponents: MessagesControllerComponents,
                                                val appConfig: FrontendAppConfig,
                                                trustStoreConnector: TrustStoreConnector,
                                                trustService: TrustService,
                                                addAnotherFormProvider: AddAnOtherIndividualFormProvider,
                                                yesNoFormProvider: YesNoFormProvider,
                                                repository: PlaybackRepository,
                                                addAnotherView: AddAnOtherIndividualView,
                                                yesNoView: AddAnOtherIndividualYesNoView,
                                                completeView: MaxedOutOtherIndividualsView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val addAnotherForm : Form[AddAnOtherIndividual] = addAnotherFormProvider()

  val yesNoForm: Form[Boolean] = yesNoFormProvider.withPrefix("addAnOtherIndividualYesNo")

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      for {
        otherIndividuals <- trustService.getOtherIndividuals(request.userAnswers.utr)
        updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
        _ <- repository.set(updatedAnswers)
      } yield {
        val otherIndividualRows = new AddAnOtherIndividualViewHelper(otherIndividuals.otherIndividuals).rows

        otherIndividuals.size match {
          case 0 =>
            Ok(yesNoView(yesNoForm))
          case _ if otherIndividuals.isNotMaxedOut =>
            Ok(addAnotherView(
              form = addAnotherForm,
              inProgressOtherIndividuals = otherIndividualRows.inProgress,
              completeOtherIndividuals = otherIndividualRows.complete,
              heading = otherIndividuals.addToHeading
            ))
          case _ if otherIndividuals.isMaxedOut =>
            Ok(completeView(
              inProgressOtherIndividuals = otherIndividualRows.inProgress,
              completeOtherIndividuals = otherIndividualRows.complete,
              heading = otherIndividuals.addToHeading
            ))
        }
      }
  }

  def submitOne(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      yesNoForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(yesNoView(formWithErrors)))
        },
        addNow => {
          if (addNow) {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
              _ <- repository.set(updatedAnswers)
            } yield Redirect(controllers.routes.InterruptPageController.onPageLoad())
          } else {
            for {
              _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
            } yield {
              Redirect(appConfig.maintainATrustOverview)
            }
          }
        }
      )
  }

  def submitAnother(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      trustService.getOtherIndividuals(request.userAnswers.utr).flatMap { otherIndividuals =>
        addAnotherForm.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {

            val rows = new AddAnOtherIndividualViewHelper(otherIndividuals.otherIndividuals).rows

            Future.successful(BadRequest(
              addAnotherView(
                formWithErrors,
                rows.inProgress,
                rows.complete,
                otherIndividuals.addToHeading
              )
            ))
          },
          {
            case AddAnOtherIndividual.YesNow =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
                _ <- repository.set(updatedAnswers)
              } yield Redirect(controllers.routes.NameController.onPageLoad(NormalMode))

            case AddAnOtherIndividual.YesLater =>
              Future.successful(Redirect(appConfig.maintainATrustOverview))

            case AddAnOtherIndividual.NoComplete =>
              for {
                _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
              } yield {
                Redirect(appConfig.maintainATrustOverview)
              }
          }
        )
      }
  }

  def submitComplete(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      for {
        _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
      } yield {
        Redirect(appConfig.maintainATrustOverview)
      }
  }
}
