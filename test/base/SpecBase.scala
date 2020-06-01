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

package base

import java.time.LocalDate

import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import navigation.FakeNavigator
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.{TestSuite, TryValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.libs.json.Json
import play.api.mvc.BodyParsers
import play.api.test.FakeRequest
import repositories.PlaybackRepository
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments}

import scala.concurrent.{ExecutionContext, Future}

trait SpecBaseHelpers extends GuiceOneAppPerSuite with TryValues with ScalaFutures with IntegrationPatience with MockitoSugar {
  this: TestSuite =>

  val userAnswersId = "id"

  val fakeNavigator = new FakeNavigator()

  def emptyUserAnswers = UserAnswers(userAnswersId, "UTRUTRUTR", LocalDate.now(), Json.obj())

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  val bodyParsers = injector.instanceOf[BodyParsers.Default]

  def fakeRequest = FakeRequest("", "")

  implicit def executionContext = injector.instanceOf[ExecutionContext]

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  val playbackRepository: PlaybackRepository = mock[PlaybackRepository]

  when(playbackRepository.set(any())).thenReturn(Future.successful(true))

  protected def applicationBuilder(userAnswers: Option[models.UserAnswers] = None,
                                   affinityGroup: AffinityGroup = AffinityGroup.Organisation,
                                   enrolments: Enrolments = Enrolments(Set.empty[Enrolment])
                                  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(bodyParsers, affinityGroup)),
        bind[PlaybackIdentifierAction].toInstance(new FakePlaybackIdentifierAction()),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[PlaybackRepository].toInstance(playbackRepository)
      )
}

trait SpecBase extends PlaySpec with SpecBaseHelpers
