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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.{OtherIndividual, OtherIndividuals, RemoveOtherIndividual, TrustDetails}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config : FrontendAppConfig) {

  private def getTrustDetailsUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/trust-details"

  def getTrustDetails(utr: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] =
    http.GET[TrustDetails](getTrustDetailsUrl(utr))

  private def getOtherIndividualsUrl(utr: String) = s"${config.trustsUrl}/trusts/$utr/transformed/other-individuals"

  def getOtherIndividuals(utr: String)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[OtherIndividuals] =
    http.GET[OtherIndividuals](getOtherIndividualsUrl(utr))

  private def addOtherIndividualUrl(utr: String) = s"${config.trustsUrl}/trusts/other-individuals/add/$utr"

  def addOtherIndividual(utr: String, otherIndividual: OtherIndividual)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    http.POST[JsValue, HttpResponse](addOtherIndividualUrl(utr), Json.toJson(otherIndividual))

  private def amendOtherIndividualUrl(utr: String, index: Int) = s"${config.trustsUrl}/trusts/other-individuals/amend/$utr/$index"

  def amendOtherIndividual(utr: String, index: Int, otherIndividual: OtherIndividual)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    http.POST[JsValue, HttpResponse](amendOtherIndividualUrl(utr, index), Json.toJson(otherIndividual))

  private def removeOtherIndividualUrl(utr: String) = s"${config.trustsUrl}/trusts/other-individuals/$utr/remove"

  def removeOtherIndividual(utr: String, otherIndividual: RemoveOtherIndividual)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    http.PUT[JsValue, HttpResponse](removeOtherIndividualUrl(utr), Json.toJson(otherIndividual))
}
