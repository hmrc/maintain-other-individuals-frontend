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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.{OtherIndividual, OtherIndividuals, RemoveOtherIndividual, TrustDetails}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config: FrontendAppConfig) {
  
  private val trustsUrl: String = s"${config.trustsUrl}/trusts"
  private val otherIndividualsUrl: String = s"$trustsUrl/other-individuals"

  def getTrustDetails(identifier: String)
                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val url: String = s"$trustsUrl/trust-details/$identifier/transformed"
    http.GET[TrustDetails](url)
  }

  def getOtherIndividuals(identifier: String)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OtherIndividuals] = {
    val url: String = s"$otherIndividualsUrl/$identifier/transformed"
    http.GET[OtherIndividuals](url)
  }

  def addOtherIndividual(identifier: String, otherIndividual: OtherIndividual)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$otherIndividualsUrl/add/$identifier"
    http.POST[JsValue, HttpResponse](url, Json.toJson(otherIndividual))
  }

  def amendOtherIndividual(identifier: String, index: Int, otherIndividual: OtherIndividual)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$otherIndividualsUrl/amend/$identifier/$index"
    http.POST[JsValue, HttpResponse](url, Json.toJson(otherIndividual))
  }

  def removeOtherIndividual(identifier: String, otherIndividual: RemoveOtherIndividual)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$otherIndividualsUrl/$identifier/remove"
    http.PUT[JsValue, HttpResponse](url, Json.toJson(otherIndividual))
  }

  def isTrust5mld(identifier: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url: String = s"$trustsUrl/$identifier/is-trust-5mld"
    http.GET[Boolean](url)
  }
  
}
