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

package connectors

import config.FrontendAppConfig
import models.{OtherIndividual, OtherIndividuals, RemoveOtherIndividual, TrustDetails}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject() (http: HttpClientV2, config: FrontendAppConfig) {

  private val trustsUrl: String           = s"${config.trustsUrl}/trusts"
  private val otherIndividualsUrl: String = s"$trustsUrl/other-individuals"

  def getTrustDetails(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val fullUrl: String = s"$trustsUrl/trust-details/$identifier/transformed"
    http.get(url"$fullUrl").execute[TrustDetails]
  }

  def getOtherIndividuals(
    identifier: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OtherIndividuals] = {
    val fullUrl: String = s"$otherIndividualsUrl/$identifier/transformed"
    http.get(url"$fullUrl").execute[OtherIndividuals]
  }

  def addOtherIndividual(identifier: String, otherIndividual: OtherIndividual)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val fullUrl: String = s"$otherIndividualsUrl/add/$identifier"
    http
      .post(url"$fullUrl")
      .withBody(Json.toJson(otherIndividual))
      .execute[HttpResponse]
  }

  def amendOtherIndividual(identifier: String, index: Int, otherIndividual: OtherIndividual)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val fullUrl: String = s"$otherIndividualsUrl/amend/$identifier/$index"
    http
      .post(url"$fullUrl")
      .withBody(Json.toJson(otherIndividual))
      .execute[HttpResponse]
  }

  def removeOtherIndividual(identifier: String, otherIndividual: RemoveOtherIndividual)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val fullUrl: String = s"$otherIndividualsUrl/$identifier/remove"
    http
      .put(url"$fullUrl")
      .withBody(Json.toJson(otherIndividual))
      .execute[HttpResponse]
  }

  def isTrust5mld(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val fullUrl: String = s"$trustsUrl/$identifier/is-trust-5mld"
    http.get(url"$fullUrl").execute[Boolean]
  }

}
