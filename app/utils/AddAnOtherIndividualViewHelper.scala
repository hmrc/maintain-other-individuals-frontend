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

package utils

import models.OtherIndividual
import play.api.i18n.Messages
import viewmodels.addAnother.{AddRow, AddToRows}

class AddAnOtherIndividualViewHelper(otherIndividuals: List[OtherIndividual])(implicit messages: Messages) {

  private def otherIndividualRow(otherIndividual: OtherIndividual, index: Int): AddRow = {
    AddRow(
      name = otherIndividual.name.displayName,
      typeLabel = messages("entities.otherIndividual"),
      changeLabel = messages("site.change.details"),
      changeUrl = Some(controllers.individual.amend.routes.CheckDetailsController.extractAndRender(index).url),
      removeLabel =  messages("site.delete"),
      removeUrl = Some(controllers.individual.remove.routes.RemoveOtherIndividualController.onPageLoad(index).url)
    )
  }

  def rows: AddToRows = {
    val complete =
      otherIndividuals.zipWithIndex.map(x => otherIndividualRow(x._1, x._2))

    AddToRows(Nil, complete)
  }

}
