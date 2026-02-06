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

package views.components

import base.SpecBase
import play.api.i18n.{Lang, MessagesApi}

class LinkSpec extends SpecBase {
  private val messageApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "Link component" should {

    "render as a GOV.UK link" in {
      val application = applicationBuilder().build()

      val view = application.injector.instanceOf[views.html.components.Link]

      val result = view.apply("link", "id", "pageNotFound.link")(messages)
      println(result.body)
      result.body must include("govuk-link")
      result.body must include(messageApi("pageNotFound.link")(Lang("en")))
    }

  }

}
