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

package services

import com.google.inject.ImplementedBy
import connectors.TrustConnector
import javax.inject.Inject
import models.{OtherIndividual, OtherIndividuals, RemoveOtherIndividual}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}


class TrustServiceImpl @Inject()(connector: TrustConnector) extends TrustService {

  override def getOtherIndividuals(utr: String)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[OtherIndividuals] =
    connector.getOtherIndividuals(utr)

  override def getOtherIndividual(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[OtherIndividual] =
    getOtherIndividuals(utr).map(_.otherIndividuals(index))

  override def removeOtherIndividual(utr: String, otherIndividual: RemoveOtherIndividual)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    connector.removeOtherIndividual(utr, otherIndividual)

}

@ImplementedBy(classOf[TrustServiceImpl])
trait TrustService {

  def getOtherIndividuals(utr: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OtherIndividuals]

  def getOtherIndividual(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[OtherIndividual]

  def removeOtherIndividual(utr: String, otherIndividual: RemoveOtherIndividual)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[HttpResponse]
}
