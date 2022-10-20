/*
 * Copyright 2022 HM Revenue & Customs
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

package repositories

import com.google.inject.ImplementedBy
import models.UserAnswers

import scala.concurrent.Future

@ImplementedBy(classOf[PlaybackRepositoryImpl])
trait PlaybackRepository {

  def get(internalId: String, utr: String, sessionId: String): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers): Future[Boolean]

}
