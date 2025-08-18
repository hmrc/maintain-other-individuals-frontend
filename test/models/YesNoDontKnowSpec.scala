/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBase

class YesNoDontKnowSpec extends SpecBase {

  "YesNoDontKnow" when {

    ".fromBoolean" should {
      "return Yes when give Some(false)" in {
        YesNoDontKnow.fromBoolean(Some(false)) mustBe Some(YesNoDontKnow.Yes)
      }

      "return No when give Some(true)" in {
        YesNoDontKnow.fromBoolean(Some(true)) mustBe Some(YesNoDontKnow.No)
      }

      "return DontKnow when give no value" in {
        YesNoDontKnow.fromBoolean(None) mustBe Some(YesNoDontKnow.DontKnow)
      }
    }
  }

}
