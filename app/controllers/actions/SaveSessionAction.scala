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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import models.UtrSession
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.{ActionFilter, Result}
import repositories.ActiveSessionRepository
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class SaveActiveSessionImpl @Inject()(utr: String,
                                      activeSessionRepository: ActiveSessionRepository
                                     )(override implicit val executionContext: ExecutionContext) extends SaveSessionAction with Logging {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {

    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val session = UtrSession(request.user.internalId, utr)

    activeSessionRepository.set(session) map { _ =>
      logger.info(s"[Session][UTR: $utr][Session ID: ${utils.Session.id(hc)}] user is starting to maintain other individuals")
      None
    }
  }
}

@ImplementedBy(classOf[SaveActiveSessionImpl])
trait SaveSessionAction extends ActionFilter[IdentifierRequest]

class SaveActiveSessionProvider @Inject()(activeSessionRepository: ActiveSessionRepository)
                                         (implicit ec: ExecutionContext) {

  def apply(utr: String) = new SaveActiveSessionImpl(utr, activeSessionRepository)
}