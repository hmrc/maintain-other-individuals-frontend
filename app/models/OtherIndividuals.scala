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

package models

import play.api.i18n.{Messages, MessagesProvider}
import play.api.libs.json.{Reads, __}

case class OtherIndividuals(otherIndividuals: List[OtherIndividual]) {

  val size: Int = otherIndividuals.size

  def addToHeading()(implicit mp: MessagesProvider): String =

    size match {
      case 0 => Messages("addAnOtherIndividual.heading")
      case 1 => Messages("addAnOtherIndividual.singular.heading")
      case l => Messages("addAnOtherIndividual.count.heading", l)
    }

  val isMaxedOut: Boolean = size >= 25

  val isNotMaxedOut: Boolean = !isMaxedOut
}

object OtherIndividuals {

  implicit val reads: Reads[OtherIndividuals] =
    (__ \ "naturalPerson")
      .readWithDefault[List[OtherIndividual]](Nil)
      .map(otherIndividuals => OtherIndividuals(otherIndividuals))

}
