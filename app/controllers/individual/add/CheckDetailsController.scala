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

import config.FrontendAppConfig
import connectors.TrustConnector
import controllers.actions._
import handlers.ErrorHandler
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.mappers.OtherIndividualMapper
import utils.print.OtherIndividualPrintHelper
import viewmodels.AnswerSection
import views.html.individual.add.CheckDetailsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  view: CheckDetailsView,
  connector: TrustConnector,
  val appConfig: FrontendAppConfig,
  printHelper: OtherIndividualPrintHelper,
  mapper: OtherIndividualMapper,
  nameAction: NameRequiredAction,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) { implicit request =>
    val section: AnswerSection = printHelper(request.userAnswers, adding = true, request.otherIndividual)
    Ok(view(Seq(section)))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForUtr.async { implicit request =>
    mapper(request.userAnswers) match {
      case None                  =>
        logger.error(
          s"[Check Details Individual][UTR: ${request.userAnswers.identifier}][Session ID: ${utils.Session.id(hc)}]" +
            s" unable to map user answers to OtherIndividual due to errors"
        )
        errorHandler.internalServerErrorTemplate(request.request).map(html => InternalServerError(html))
      case Some(otherIndividual) =>
        connector
          .addOtherIndividual(request.userAnswers.identifier, otherIndividual)
          .map(_ => Redirect(controllers.routes.AddAnOtherIndividualController.onPageLoad()))
    }
  }

}
