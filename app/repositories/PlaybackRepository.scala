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

package repositories

import models.UserAnswers
import play.api.Configuration
import play.api.libs.json._
import reactivemongo.api.WriteConcern
import reactivemongo.api.bson.collection.BSONSerializationPack
import reactivemongo.api.indexes.{Index, IndexType}

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PlaybackRepositoryImpl @Inject()(
                                        mongo: MongoDriver,
                                        config: Configuration
                                      )(implicit ec: ExecutionContext) extends IndexesManager(mongo, config) with PlaybackRepository {

  override val collectionName: String = "user-answers"

  override val cacheTtl: Int = config.get[Int]("mongodb.playback.ttlSeconds")

  override val lastUpdatedIndexName: String = "user-answers-updated-at-index"

  override def idIndex: Index.Aux[BSONSerializationPack.type] = MongoIndex(
    key = Seq("internalId" -> IndexType.Ascending, "utr" -> IndexType.Ascending),
    name = "internal-id-and-utr-compound-index"
  )

  private def selector(internalId: String, utr: String): JsObject = Json.obj(
    "internalId" -> internalId,
    "utr" -> utr
  )

  override def get(internalId: String, utr: String): Future[Option[UserAnswers]] = {
    findCollectionAndUpdate[UserAnswers](selector(internalId, utr))
  }

  override def set(userAnswers: UserAnswers): Future[Boolean] = {

    val modifier = Json.obj(
      "$set" -> userAnswers.copy(updatedAt = LocalDateTime.now)
    )

    for {
      col <- collection
      r <- col.update(ordered = false).one(selector(userAnswers.internalId, userAnswers.identifier), modifier, upsert = true, multi = false)
    } yield r.ok
  }

  override def resetCache(internalId: String, utr: String): Future[Option[JsObject]] = {
    for {
      col <- collection
      r <- col.findAndRemove(selector(internalId, utr), None, None, WriteConcern.Default, None, None, Seq.empty)
    } yield r.value
  }
}

trait PlaybackRepository {

  def get(internalId: String, utr: String): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers): Future[Boolean]

  def resetCache(internalId: String, utr: String): Future[Option[JsObject]]
}

