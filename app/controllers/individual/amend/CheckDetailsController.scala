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

package controllers.individual.amend

import config.FrontendAppConfig
import connectors.TrustConnector
import controllers.actions._
import extractors.OtherIndividualExtractor
import handlers.ErrorHandler
import javax.inject.Inject
import models.UserAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.mappers.OtherIndividualMapper
import utils.print.OtherIndividualPrintHelper
import viewmodels.AnswerSection
import views.html.individual.amend.CheckDetailsView

import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  view: CheckDetailsView,
  service: TrustService,
  connector: TrustConnector,
  val appConfig: FrontendAppConfig,
  playbackRepository: PlaybackRepository,
  printHelper: OtherIndividualPrintHelper,
  mapper: OtherIndividualMapper,
  nameAction: NameRequiredAction,
  extractor: OtherIndividualExtractor,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  private def render(userAnswers: UserAnswers, index: Int, name: String)(implicit
    request: Request[AnyContent]
  ): Result = {
    val section: AnswerSection = printHelper(userAnswers, adding = false, name)
    Ok(view(Seq(section), index))
  }

  def extractAndRender(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async { implicit request =>
    service.getOtherIndividual(request.userAnswers.identifier, index) flatMap { individual =>
      val extractedAnswers = extractor(request.userAnswers, individual, index)
      for {
        extractedF <- Future.fromTry(extractedAnswers)
        _          <- playbackRepository.set(extractedF)
      } yield render(extractedF, index, individual.name.displayName)
    } recoverWith { case _ =>
      logger.error(
        s"[Amend Individual][UTR: ${request.userAnswers.identifier}][Session ID: ${utils.Session.id(hc)}]" +
          s" no other individual found in trusts service to maintain"
      )
      errorHandler.internalServerErrorTemplate.map(res => InternalServerError(res))
    }
  }

  def renderFromUserAnswers(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.andThen(nameAction) {
    implicit request =>
      render(request.userAnswers, index, request.otherIndividual)
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForUtr.async { implicit request =>
    mapper(request.userAnswers).map { individual =>
      connector
        .amendOtherIndividual(request.userAnswers.identifier, index, individual)
        .map(_ => Redirect(controllers.routes.AddAnOtherIndividualController.onPageLoad()))
    } getOrElse {
      logger.error(
        s"[Amend Individual][UTR: ${request.userAnswers.identifier}][Session ID: ${utils.Session.id(hc)}]" +
          s" unable to submit amended other individual due to problems mapping user answers"
      )
      errorHandler.internalServerErrorTemplate.map(res => InternalServerError(res))
    }
  }

}
