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

package controllers

import base.SpecBase
import connectors.TrustsStoreConnector
import forms.{AddAnOtherIndividualFormProvider, YesNoFormProvider}
import models.TaskStatus.Completed
import models.{
  AddAnOtherIndividual, Name, NationalInsuranceNumber, NormalMode, OtherIndividual, OtherIndividuals,
  RemoveOtherIndividual, UserAnswers
}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Headers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.AddAnOtherIndividualViewHelper
import viewmodels.addAnother.AddRow
import views.html.{AddAnOtherIndividualView, AddAnOtherIndividualYesNoView, MaxedOutOtherIndividualsView}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class AddAnOtherIndividualControllerSpec extends SpecBase with ScalaFutures with BeforeAndAfterEach {

  lazy val getRoute: String            = controllers.routes.AddAnOtherIndividualController.onPageLoad().url
  lazy val submitOneRoute: String      = controllers.routes.AddAnOtherIndividualController.submitOne().url
  lazy val submitAnotherRoute: String  = controllers.routes.AddAnOtherIndividualController.submitAnother().url
  lazy val submitCompleteRoute: String = controllers.routes.AddAnOtherIndividualController.submitComplete().url

  val mockStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]

  val addOtherIndividualForm: Form[AddAnOtherIndividual] = new AddAnOtherIndividualFormProvider()()
  val addOtherIndividualYesNoForm: Form[Boolean]         = new YesNoFormProvider().withPrefix("addAnOtherIndividualYesNo")

  private val otherIndividual = OtherIndividual(
    name = Name(firstName = "First", middleName = None, lastName = "Last"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    countryOfNationality = None,
    countryOfResidence = None,
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    mentalCapacityYesNo = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  private val otherIndividuals = OtherIndividuals(List(otherIndividual))

  val otherIndividualRows: List[AddRow] = List(
    AddRow(
      "First Last",
      typeLabel = "Other individual",
      "Change details",
      Some(controllers.individual.amend.routes.CheckDetailsController.extractAndRender(0).url),
      "Remove",
      Some(controllers.individual.remove.routes.RemoveOtherIndividualController.onPageLoad(0).url)
    )
  )

  class FakeService(data: OtherIndividuals) extends TrustService {

    override def getOtherIndividuals(
      utr: String
    )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OtherIndividuals] =
      Future.successful(data)

    override def getOtherIndividual(utr: String, index: Int)(implicit
      hc: HeaderCarrier,
      ex: ExecutionContext
    ): Future[OtherIndividual] =
      Future.successful(otherIndividual)

    override def removeOtherIndividual(utr: String, otherIndividual: RemoveOtherIndividual)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[HttpResponse] =
      Future.successful(HttpResponse(OK, ""))

    override def getIndividualNinos(identifier: String, index: Option[Int])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[List[String]] =
      ???

  }

  override def beforeEach(): Unit = {
    reset(mockStoreConnector)

    when(mockStoreConnector.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse.apply(OK, "")))
  }

  "AddAnOtherIndividual Controller" when {

    "no other individuals" must {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", AddAnOtherIndividual.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAnOtherIndividualYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            addOtherIndividualYesNoForm
          )(request, messages).toString

        application.stop()
      }

      "redirect to the maintain task list when the user answers no" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          )
          .build()

        val request = FakeRequest(POST, submitOneRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

        application.stop()
      }

      "redirect to interrupt page when the user answers yes" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          )
          .build()

        val request = FakeRequest(POST, submitOneRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.InterruptPageController.taxablePageLoad().url

        application.stop()
      }

      "redirect to non taxable information page when the user answers yes" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val userAns = UserAnswers(
          userAnswersId,
          "UTRUTRUTR",
          "sessionId",
          s"$userAnswersId-UTRUTRUTR-sessionId",
          LocalDate.now(),
          isTaxable = false
        )

        val application = applicationBuilder(userAnswers = Some(userAns))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          )
          .build()

        val request = FakeRequest(POST, submitOneRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.InterruptPageController.nonTaxablePageLoad().url

        application.stop()
      }

    }

    "there are other individuals" must {

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAnOtherIndividualView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            addOtherIndividualForm,
            Nil,
            otherIndividualRows,
            "Add another individual"
          )(request, messages).toString

        application.stop()
      }

      "redirect to the maintain task list when the user says they are done" in {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          )
          .build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", AddAnOtherIndividual.NoComplete.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

        application.stop()
      }

      "redirect to name page when the user answers yes now" in {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          )
          .build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", AddAnOtherIndividual.YesNow.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.individual.routes.NameController.onPageLoad(NormalMode).url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val otherIndividuals    = OtherIndividuals(List.fill(2)(otherIndividual))
        val fakeService         = new FakeService(otherIndividuals)
        val otherIndividualRows = new AddAnOtherIndividualViewHelper(otherIndividuals.otherIndividuals).rows

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, submitAnotherRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addOtherIndividualForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAnOtherIndividualView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            otherIndividualRows.inProgress,
            otherIndividualRows.complete,
            "The trust has 2 individuals"
          )(request, messages).toString

        application.stop()
      }
    }

    "maxed out other individuals" must {

      "return OK and the correct view for a GET" in {

        val otherIndividuals = OtherIndividuals(List.fill(25)(otherIndividual))

        val fakeService = new FakeService(otherIndividuals)

        val otherIndividualRows = new AddAnOtherIndividualViewHelper(otherIndividuals.otherIndividuals).rows

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaxedOutOtherIndividualsView]

        status(result) mustEqual OK

        val content = contentAsString(result)

        content mustEqual
          view(
            otherIndividualRows.inProgress,
            otherIndividualRows.complete,
            otherIndividuals.addToHeading()
          )(request, messages).toString
        content must include("You cannot enter another individual as you have entered a maximum of 25.")
        content must include("If you have further individuals to add, write to HMRC with their details.")

        application.stop()

      }

      "redirect to add to page and set other individuals to complete when user clicks continue" in {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          )
          .build()

        val request = FakeRequest(POST, submitCompleteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

        application.stop()

      }
    }

    "toggling language from an error state" must {

      "redirect to GET /add-another" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val submitRoute = submitAnotherRoute

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val submitEmptyFormRequest = FakeRequest(POST, submitRoute).withFormUrlEncodedBody(("value", ""))
        val submitEmptyFormResult  = route(application, submitEmptyFormRequest).value
        status(submitEmptyFormResult) mustEqual BAD_REQUEST

        val toggleLanguageRoute: String = routes.LanguageSwitchController.switchToLanguage("cymraeg").url
        val headers: Headers            = new Headers(Seq(("Referer", submitRoute)))
        val toggleLanguageRequest       = FakeRequest(GET, toggleLanguageRoute).withHeaders(headers)
        val toggleLanguageResult        = route(application, toggleLanguageRequest).value
        status(toggleLanguageResult) mustEqual SEE_OTHER

        val referrerRoute   = redirectLocation(toggleLanguageResult).value
        val referrerRequest = FakeRequest(GET, referrerRoute)
        val referrerResult  = route(application, referrerRequest).value
        status(referrerResult) mustEqual OK

        application.stop()
      }
    }
  }

}
